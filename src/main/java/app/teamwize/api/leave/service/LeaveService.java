package app.teamwize.api.leave.service;

import app.teamwize.api.auth.domain.event.UserEventPayload;
import app.teamwize.api.base.domain.model.request.PaginationRequest;
import app.teamwize.api.event.service.EventService;
import app.teamwize.api.holiday.domain.entity.Holiday;
import app.teamwize.api.holiday.service.HolidayService;
import app.teamwize.api.leave.exception.LeaveNotFoundException;
import app.teamwize.api.leave.exception.LeavePolicyNotFoundException;
import app.teamwize.api.leave.exception.LeaveTypeNotFoundException;
import app.teamwize.api.leave.exception.LeaveUpdateStatusFailedException;
import app.teamwize.api.leave.model.LeaveBalance;
import app.teamwize.api.leave.model.LeaveCheckResult;
import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.leave.model.LeaveVoteStatus;
import app.teamwize.api.leave.model.command.LeaveCheckCommand;
import app.teamwize.api.leave.model.command.LeaveCreateCommand;
import app.teamwize.api.leave.model.command.LeaveVoteCommand;
import app.teamwize.api.leave.model.entity.Leave;
import app.teamwize.api.leave.model.entity.LeavePolicyActivatedTypeId;
import app.teamwize.api.leave.model.entity.LeaveVote;
import app.teamwize.api.leave.model.event.LeaveCreatedEvent;
import app.teamwize.api.leave.model.event.LeaveEventPayload;
import app.teamwize.api.leave.model.event.LeaveStatusUpdatedEvent;
import app.teamwize.api.leave.model.event.LeaveVotedEvent;
import app.teamwize.api.leave.repository.LeaveRepository;
import app.teamwize.api.leave.repository.LeaveSpecifications;
import app.teamwize.api.leave.repository.LeaveVoteRepository;
import app.teamwize.api.leave.rest.model.request.LeaveFilterRequest;
import app.teamwize.api.organization.domain.entity.Organization;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
import app.teamwize.api.user.domain.UserRole;
import app.teamwize.api.user.domain.entity.User;
import app.teamwize.api.user.exception.UserNotFoundException;
import app.teamwize.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static app.teamwize.api.leave.repository.LeaveSpecifications.*;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserService userService;
    private final HolidayService holidayService;
    private final OrganizationService organizationService;
    private final LeavePolicyService leavePolicyService;
    private final EventService eventService;
    private final LeaveVoteRepository leaveVoteRepository;

    @Transactional
    public Leave createLeave(Long organizationId, Long userId, LeaveCreateCommand command) throws UserNotFoundException, LeaveTypeNotFoundException, OrganizationNotFoundException, LeavePolicyNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        var user = userService.getUser(organizationId, userId);
        var leavePolicy = leavePolicyService.getLeavePolicy(organizationId, user.getLeavePolicy().getId());

        var activatedType = leavePolicy.getActivatedTypes().stream()
                .filter(type -> type.getId().equals(new LeavePolicyActivatedTypeId(leavePolicy.getId(), command.typeId())))
                .findFirst()
                .orElseThrow(() -> new LeaveTypeNotFoundException("Leave type not found with id: " + command.typeId()));

        var dayOff = new Leave()
                .setReason(command.reason())
                .setStartAt(command.start())
                .setEndAt(command.end())
                .setUser(user)
                .setOrganization(organization)
                .setStatus(LeaveStatus.PENDING)
                .setActivatedType(activatedType)
                .setPolicy(leavePolicy)
                .setType(activatedType.getType())
                .setDuration(calculateLeaveDuration(organization, user, command.start(), command.end()));
        dayOff = leaveRepository.persist(dayOff);


        eventService.emmit(organizationId, new LeaveCreatedEvent(new LeaveEventPayload(dayOff), new UserEventPayload(user)));

        return dayOff;
    }

    public Page<Leave> getLeaves(Long organizationId, LeaveFilterRequest filters, PaginationRequest pagination) {
        var sort = Sort.by("id").descending();
        var pageRequest = PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), sort);

        var specs = hasOrganizationId(organizationId);


        if (filters.teamId() != null) {
            specs = specs.and(hasTeamId(filters.teamId()));
        }
        if (filters.userId() != null) {
            specs = specs.and(hasUserId(filters.userId()));
        }
        if (filters.status() != null) {
            specs = specs.and(hasStatus(filters.status()));
        }
        if (filters.start() != null && filters.end() != null) {
            specs = specs.and(LeaveSpecifications.isStartedInBetween(filters.start(), filters.end()).or(LeaveSpecifications.isEndedInBetween(filters.start(), filters.end())));
        }
        return leaveRepository.findAll(specs, pageRequest);

    }

    public Page<Leave> getLeaves(Long organizationId, Long userId, PaginationRequest pagination) {
        var paging = PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), Sort.by("id").descending());
        return leaveRepository.findByOrganizationIdAndUserId(organizationId, userId, paging);
    }

    @Transactional
    public Leave vote(Long organizationId, Long leaveId, Long approverId, LeaveVoteCommand command) throws LeaveNotFoundException, LeavePolicyNotFoundException, UserNotFoundException, LeaveUpdateStatusFailedException {
        // Get approver and validate role
        var approver = userService.getUser(organizationId, approverId);
        boolean isAdmin = approver.getRole() == UserRole.ORGANIZATION_ADMIN || approver.getRole() == UserRole.TEAM_ADMIN;
        if (!isAdmin) {
            throw new LeaveUpdateStatusFailedException("Leave update failed because user is not authorized to update leave, id = " + leaveId);
        }

        // Get leave and validate status
        var leave = leaveRepository.findByOrganizationIdAndId(organizationId, leaveId)
                .orElseThrow(() -> new LeaveNotFoundException("Leave not found with id: " + leaveId));
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new LeaveUpdateStatusFailedException("Leave update failed because it is not in pending status, id = " + leaveId);
        }

        // Get policy and validate approver permission
        var policy = leavePolicyService.getLeavePolicy(organizationId, leave.getPolicy().getId());
        var policyApprovers = policy.getApprovers();
        boolean isPolicyApprover = policyApprovers.stream()
                .anyMatch(approverItem -> approverItem.getUser().getId().equals(approverId));
        if (!isPolicyApprover) {
            throw new LeaveUpdateStatusFailedException("Leave update failed because user is not authorized to update leave, id = " + leaveId);
        }

        // Record the vote
        var vote = new LeaveVote()
                .setLeaveId(leaveId)
                .setUserId(approverId)
                .setStatus(command.status());
        leaveVoteRepository.persist(vote);

        // Evaluate approval steps
        var leaveVotes = leaveVoteRepository.findByLeaveId(leaveId);
        var approverIds = policyApprovers.stream()
                .map(a -> a.getUser().getId())
                .toList();

        var approvalStepsResult = policy.getApprovalSteps().stream().map(step -> {
            var relevantVotes = leaveVotes.stream()
                    .filter(v -> approverIds.contains(v.getUserId()))
                    .toList();
            long acceptedVotes = relevantVotes.stream()
                    .filter(v -> v.getStatus() == LeaveVoteStatus.ACCEPTED)
                    .count();
            long rejectedVotes = relevantVotes.stream()
                    .filter(v -> v.getStatus() == LeaveVoteStatus.REJECTED)
                    .count();

            return switch (step.getCondition()) {
                case ANY -> (acceptedVotes > 0) ? LeaveStatus.ACCEPTED :
                        (rejectedVotes > 0) ? LeaveStatus.REJECTED :
                                LeaveStatus.PENDING;
                case ALL -> (acceptedVotes == approverIds.size()) ? LeaveStatus.ACCEPTED :
                        (rejectedVotes > 0) ? LeaveStatus.REJECTED :
                                LeaveStatus.PENDING;
            };
        }).toList();

        // Emit vote event
        eventService.emmit(organizationId, new LeaveVotedEvent(
                new LeaveEventPayload(leave),
                new UserEventPayload(approver),
                command.status()));

        // Determine final leave status
        if (approvalStepsResult.stream().allMatch(status -> status == LeaveStatus.ACCEPTED)) {
            leave.setStatus(LeaveStatus.ACCEPTED);
            eventService.emmit(organizationId, new LeaveStatusUpdatedEvent(
                    new LeaveEventPayload(leave),
                    new UserEventPayload(approver)));
        } else if (approvalStepsResult.stream().anyMatch(status -> status == LeaveStatus.REJECTED)) {
            leave.setStatus(LeaveStatus.REJECTED);
            eventService.emmit(organizationId, new LeaveStatusUpdatedEvent(
                    new LeaveEventPayload(leave),
                    new UserEventPayload(approver)));
        }

        return leaveRepository.update(leave);
    }

    public Leave getLeave(Long organizationId, Long id) throws LeaveNotFoundException {
        return getById(organizationId, id);
    }

    public Float getTotalDuration(Long organizationId, Long userId, LeavePolicyActivatedTypeId activatedTypeId, LeaveStatus status) {
        var sum = leaveRepository.countByOrganizationIdAndUserIdAndTypeId(organizationId, userId, activatedTypeId, status);
        return sum != null ? sum : 0f;
    }

    private Leave getById(Long OrganizationId, Long id) throws LeaveNotFoundException {
        return leaveRepository.findByOrganizationIdAndId(OrganizationId, id).orElseThrow(() -> new LeaveNotFoundException("Leave not found with id: " + id));
    }

    public List<LeaveBalance> getLeaveBalance(Long organizationId, Long userId) throws UserNotFoundException, LeavePolicyNotFoundException {
        var user = userService.getUser(organizationId, userId);
        var policy = leavePolicyService.getLeavePolicy(organizationId, user.getLeavePolicy().getId());
        var startedAt = user.getJoinedAt();
        var result = new ArrayList<LeaveBalance>();
        for (var activatedType : policy.getActivatedTypes()) {
            var usedAmount = this.getTotalDuration(organizationId, userId, activatedType.getId(), LeaveStatus.ACCEPTED);
            var totalAmount = switch (activatedType.getType().getCycle()) {
                case UNLIMITED -> Integer.MAX_VALUE;
                case PER_MONTH ->
                        Period.between(startedAt.atZone(user.getTimeZoneId()).toLocalDate(), LocalDate.now()).toTotalMonths() * activatedType.getAmount();
                case PER_YEAR ->
                        (Period.between(startedAt.atZone(user.getTimeZoneId()).toLocalDate(), LocalDate.now()).toTotalMonths() / 12f) * activatedType.getAmount();
            };
            result.add(new LeaveBalance(activatedType, usedAmount.longValue(), (long) totalAmount, startedAt.atZone(user.getTimeZoneId()).toLocalDate()));
        }
        return result;
    }

    private Float calculateLeaveDuration(Organization organization, User user, Instant start, Instant end) {
        var startDate = start.atZone(ZoneId.of(organization.getTimezone())).toLocalDate();
        var endDate = end.atZone(ZoneId.of(organization.getTimezone())).toLocalDate();

        var holidayDates = holidayService.getHolidays(organization.getId(), startDate, endDate, user.getCountry())
                .stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        var workingDays = Arrays.stream(organization.getWorkingDays()).collect(Collectors.toSet());

        var leaveDays = startDate.datesUntil(endDate)
                .filter(date -> !holidayDates.contains(date))
                .filter(date -> workingDays.contains(date.getDayOfWeek()))
                .count();

        return (float) leaveDays;
    }

    public LeaveCheckResult checkRequestedLeave(Long organizationId, Long userId, LeaveCheckCommand command)
            throws UserNotFoundException, OrganizationNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        var user = userService.getUser(organizationId, userId);
        var startDate = command.start().atZone(user.getTimeZoneId()).toLocalDate();
        var endDate = command.end().atZone(user.getTimeZoneId()).toLocalDate();
        var currentUserLeaves = getLeaves(organizationId,
                new LeaveFilterRequest(null, userId, null, command.start(), command.end()),
                new PaginationRequest(0, 1000)
        ).getContent();
        var teamLeaves = getLeaves(organizationId,
                new LeaveFilterRequest(user.getTeam().getId(), null, LeaveStatus.ACCEPTED, command.start(), command.end()),
                new PaginationRequest(0, 1000)
        ).getContent();
        var leaveDuration = calculateLeaveDuration(organization, user, command.start(), command.end());
        var holidays = holidayService.getHolidays(organization.getId(), startDate, endDate, user.getCountry());

        return new LeaveCheckResult(
                true,
                "You are allowed to request this leave",
                leaveDuration,
                currentUserLeaves,
                teamLeaves,
                holidays
        );
    }

}
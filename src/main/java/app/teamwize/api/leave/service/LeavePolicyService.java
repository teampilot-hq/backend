package app.teamwize.api.leave.service;

import app.teamwize.api.base.domain.entity.EntityStatus;
import app.teamwize.api.leave.exception.LeavePolicyNotFoundException;
import app.teamwize.api.leave.exception.LeaveTypeNotFoundException;
import app.teamwize.api.leave.mapper.LeavePolicyMapper;
import app.teamwize.api.leave.model.LeaveApproverCondition;
import app.teamwize.api.leave.model.LeavePolicyStatus;
import app.teamwize.api.leave.model.LeaveTypeCycle;
import app.teamwize.api.leave.model.command.LeaveApproverSaveCommand;
import app.teamwize.api.leave.model.command.LeavePolicyActivatedTypeCommand;
import app.teamwize.api.leave.model.command.LeavePolicyCommand;
import app.teamwize.api.leave.model.command.LeaveTypeCommand;
import app.teamwize.api.leave.model.entity.*;
import app.teamwize.api.leave.repository.LeavePolicyActivatedTypeRepository;
import app.teamwize.api.leave.repository.LeavePolicyApproverRepository;
import app.teamwize.api.leave.repository.LeavePolicyRepository;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
import app.teamwize.api.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeavePolicyApproverRepository leavePolicyApproverRepository;
    private final LeavePolicyActivatedTypeRepository leavePolicyActivatedTypeRepository;
    private final OrganizationService organizationService;
    private final LeaveTypeService leaveTypeService;
    private final LeavePolicyMapper leavePolicyMapper;

    @Transactional
    public LeavePolicy createLeavePolicy(Long organizationId, LeavePolicyCommand leavePolicyCommand) throws OrganizationNotFoundException, LeaveTypeNotFoundException {
        var organization = organizationService.getOrganization(organizationId);

        var policy = new LeavePolicy()
                .setName(leavePolicyCommand.name())
                .setStatus(leavePolicyCommand.status())
                .setOrganization(organization)
                .setActivatedTypes(new ArrayList<>());

        for (var activatedType : leavePolicyCommand.activatedTypes()) {
            var type = leaveTypeService.getLeaveType(organizationId, activatedType.typeId());
            policy.getActivatedTypes().add(new LeavePolicyActivatedType()
                    .setId(new LeavePolicyActivatedTypeId(policy.getId(), type.getId()))
                    .setType(type)
                    .setPolicy(policy)
                    .setAmount(activatedType.amount())
                    .setRequiresApproval(activatedType.requiresApproval())
                    .setStatus(EntityStatus.ACTIVE)
            );
        }

        // Set approvers
        List<LeavePolicyApprover> approvers = leavePolicyMapper.toLeaveApprovers(leavePolicyCommand.approvers());
        approvers.forEach(approver -> approver.setPolicy(policy));
        policy.setApprovers(approvers);

        return leavePolicyRepository.merge(policy);
    }

    @Transactional
    public LeavePolicy createDefaultLeavePolicy(Long organizationId, Long organizationAdminId) throws OrganizationNotFoundException, LeaveTypeNotFoundException {
        var leaveTypes = leaveTypeService.createLeaveTypes(organizationId, List.of(
                new LeaveTypeCommand("🏖️", "Vacation", LeaveTypeCycle.PER_MONTH, 2, true),
                new LeaveTypeCommand("🧳", "PTO", LeaveTypeCycle.PER_MONTH, 2, true),
                new LeaveTypeCommand("🤒", "Sick-Leave", LeaveTypeCycle.PER_YEAR, 30, false)
        ));
        var request = new LeavePolicyCommand("Default-Policy",
                LeavePolicyStatus.DEFAULT,
                List.of(new LeavePolicyCommand.LeavePolicyApprovalStep(1, LeaveApproverCondition.ALL)),
                List.of(new LeaveApproverSaveCommand(1, organizationAdminId)),
                List.of(
                        new LeavePolicyActivatedTypeCommand(leaveTypes.get(0).getId(), 2, true),
                        new LeavePolicyActivatedTypeCommand(leaveTypes.get(1).getId(), 2, true),
                        new LeavePolicyActivatedTypeCommand(leaveTypes.get(2).getId(), 30, false)
                )
        );
        return createLeavePolicy(organizationId, request);
    }

    public List<LeavePolicy> getLeavePolicies(Long organizationId) {
        var leavePolicies = leavePolicyRepository.findByOrganizationIdAndStatusIsIn(organizationId, List.of(LeavePolicyStatus.ACTIVE, LeavePolicyStatus.DEFAULT));
        var leavePolicyIds = leavePolicies.stream().map(LeavePolicy::getId).toList();
        var leavePolicyApprovers = leavePolicyApproverRepository.findByPolicyIdIsIn(leavePolicyIds);
        var leavePolicyActivatedTypes = leavePolicyActivatedTypeRepository.findByPolicyIdIsIn(leavePolicyIds);
        for (LeavePolicy leavePolicy : leavePolicies) {
            var approvers = leavePolicyApprovers.stream()
                    .filter(leavePolicyApprover -> leavePolicyApprover.getPolicy().getId().equals(leavePolicy.getId()))
                    .collect(Collectors.toList());
            var activatedTypes = leavePolicyActivatedTypes.stream()
                    .filter(leavePolicyActivatedType -> leavePolicyActivatedType.getPolicy().getId().equals(leavePolicy.getId()))
                    .collect(Collectors.toList());
            leavePolicy
                    .setApprovers(approvers)
                    .setActivatedTypes(activatedTypes);
        }
        return leavePolicies;
    }

    public Optional<LeavePolicy> getDefaultLeavePolicy(Long organizationId) {
        var policies = leavePolicyRepository.findByOrganizationIdAndStatusIsIn(organizationId, List.of(LeavePolicyStatus.DEFAULT));
        if (policies.isEmpty()) return Optional.empty();
        return Optional.of(policies.getFirst());
    }

    @Transactional
    public void updateDefaultPolicy(Long organizationId, Long id) {
        var defaultPolicyOptional = getDefaultLeavePolicy(organizationId);
        if (defaultPolicyOptional.isPresent()) {
            var defaultPolicy = defaultPolicyOptional.get();
            defaultPolicy.setStatus(LeavePolicyStatus.ACTIVE);
            leavePolicyRepository.update(defaultPolicy);
        }
        leavePolicyRepository.updateStatus(organizationId, id, LeavePolicyStatus.ARCHIVED);
    }

    @Transactional
    public LeavePolicy updateLeavePolicy(Long organizationId, Long id, LeavePolicyCommand command) throws OrganizationNotFoundException, LeavePolicyNotFoundException, LeaveTypeNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        var policy = getLeavePolicy(organizationId, id)
                .setName(command.name())
                .setStatus(command.status())
                .setOrganization(organization);

        var activatedTypes = policy.getActivatedTypes();
        var approvers = policy.getApprovers();

        // Create a set of type IDs from the command for easy lookup
        var newTypeIds = command.activatedTypes().stream()
                .map(activatedTypeCommand -> new LeavePolicyActivatedTypeId(policy.getId(), activatedTypeCommand.typeId()))
                .collect(Collectors.toSet());

        for (var activatedType : activatedTypes) {
            if (!newTypeIds.contains(activatedType.getId())) {
                activatedType.setStatus(EntityStatus.ARCHIVED);
            }
        }

        for (var activatedTypeCommand : command.activatedTypes()) {
            var type = leaveTypeService.getLeaveType(organizationId, activatedTypeCommand.typeId());
            var activatedType = policy.getActivatedTypes().stream()
                    .filter(at -> at.getType().getId().equals(type.getId()))
                    .findFirst()
                    .orElse(new LeavePolicyActivatedType().setId(new LeavePolicyActivatedTypeId(policy.getId(), type.getId())));

            activatedType
                    .setType(type)
                    .setPolicy(policy)
                    .setAmount(activatedTypeCommand.amount())
                    .setRequiresApproval(activatedTypeCommand.requiresApproval())
                    .setStatus(EntityStatus.ACTIVE);

            if (activatedTypes.stream().noneMatch(ac -> ac.equals(activatedType))) {
                activatedTypes.add(activatedType);
            }

        }

        // Update approvers
        var newApproverIds = command.approvers().stream()
                .map(LeaveApproverSaveCommand::userId)
                .collect(Collectors.toSet());

        var removedApproverIds = approvers.stream()
                .map(a -> a.getUser().getId())
                .filter(a -> !newApproverIds.contains(a))
                .toList();

        leavePolicyApproverRepository.deleteAllByIdInBatch(removedApproverIds);
        approvers.removeIf(a -> removedApproverIds.contains(a.getUser().getId()));


        for (var approverCommand : command.approvers()) {
            var approver = approvers.stream()
                    .filter(a -> a.getUser().getId().equals(approverCommand.userId()))
                    .findFirst()
                    .orElse(new LeavePolicyApprover()
                            .setPolicy(policy)
                            .setUser(new User(approverCommand.userId()))
                            .setStep(approverCommand.step())
                    );

            approver
                    .setStep(approverCommand.step())
                    .setUser(new User(approverCommand.userId()));

            if (approver.getId() == null) {
                approvers.add(approver);
            }
        }

        approvers = leavePolicyApproverRepository.mergeAll(approvers);
        activatedTypes = leavePolicyActivatedTypeRepository.mergeAll(activatedTypes);

        policy.setApprovalSteps(command.approvalSteps().stream()
                .map(step -> new LeavePolicyApprovalStep(step.step(), step.condition()))
                .toList()
        );

        policy
                .setApprovers(approvers)
                .setActivatedTypes(activatedTypes);

        return leavePolicyRepository.merge(policy);
    }

    @Transactional
    public void deleteLeavePolicy(Long organizationId, Long id) {
        leavePolicyRepository.updateStatus(organizationId, id, LeavePolicyStatus.ARCHIVED);
    }

    public LeavePolicy getLeavePolicy(Long organizationId, Long id) throws LeavePolicyNotFoundException {
        var policy = leavePolicyRepository.findByOrganizationIdAndId(organizationId, id)
                .orElseThrow(() -> new LeavePolicyNotFoundException("Leave policy not found with id: " + id));

        var leavePolicyApprovers = leavePolicyApproverRepository.findByPolicyId(policy.getId());
        var leavePolicyActivatedTypes = leavePolicyActivatedTypeRepository.findByPolicyId(policy.getId());

        leavePolicyApprovers.forEach(leavePolicyApprover -> leavePolicyApprover.setPolicy(policy));
        leavePolicyActivatedTypes.forEach(leavePolicyActivatedType -> leavePolicyActivatedType.setPolicy(policy));

        return policy
                .setApprovers(leavePolicyApprovers)
                .setActivatedTypes(leavePolicyActivatedTypes);
    }
}

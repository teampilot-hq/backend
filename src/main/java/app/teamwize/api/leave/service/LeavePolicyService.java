package app.teamwize.api.leave.service;

import app.teamwize.api.base.domain.entity.EntityStatus;
import app.teamwize.api.leave.exception.LeavePolicyNotFoundException;
import app.teamwize.api.leave.exception.LeaveTypeNotFoundException;
import app.teamwize.api.leave.model.LeavePolicyStatus;
import app.teamwize.api.leave.model.LeaveTypeCycle;
import app.teamwize.api.leave.model.command.LeavePolicyActivatedTypeCommand;
import app.teamwize.api.leave.model.command.LeavePolicyCommand;
import app.teamwize.api.leave.model.command.LeaveTypeCommand;
import app.teamwize.api.leave.model.entity.LeavePolicy;
import app.teamwize.api.leave.model.entity.LeavePolicyActivatedType;
import app.teamwize.api.leave.model.entity.LeavePolicyActivatedTypeId;
import app.teamwize.api.leave.repository.LeavePolicyRepository;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
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
    private final OrganizationService organizationService;
    private final LeaveTypeService leaveTypeService;

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

        return leavePolicyRepository.merge(policy);
    }

    @Transactional
    public LeavePolicy createDefaultLeavePolicy(Long organizationId) throws OrganizationNotFoundException, LeaveTypeNotFoundException {
        var leaveTypes = leaveTypeService.createLeaveTypes(organizationId, List.of(
                new LeaveTypeCommand("🏖️", "Vacation", LeaveTypeCycle.PER_MONTH, 2, true),
                new LeaveTypeCommand("🧳", "PTO", LeaveTypeCycle.PER_MONTH, 2, true),
                new LeaveTypeCommand("🤒", "Sick-Leave", LeaveTypeCycle.PER_YEAR, 30, false)
        ));
        var request = new LeavePolicyCommand("Default-Policy",
                LeavePolicyStatus.DEFAULT,
                List.of(
                        new LeavePolicyActivatedTypeCommand(leaveTypes.get(0).getId(), 2, true),
                        new LeavePolicyActivatedTypeCommand(leaveTypes.get(1).getId(), 2, true),
                        new LeavePolicyActivatedTypeCommand(leaveTypes.get(2).getId(), 30, false)
                )
        );
        return createLeavePolicy(organizationId, request);
    }

    public List<LeavePolicy> getLeavePolicies(Long organizationId) {
        return leavePolicyRepository.findByOrganizationIdAndStatusIsIn(organizationId, List.of(LeavePolicyStatus.ACTIVE, LeavePolicyStatus.DEFAULT));
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

        // Create a set of type IDs from the command for easy lookup
        var newTypeIds = command.activatedTypes().stream()
                .map(activatedTypeCommand -> new LeavePolicyActivatedTypeId(policy.getId(), activatedTypeCommand.typeId()))
                .collect(Collectors.toSet());

        // Remove activated types that are not in the new command
        policy.getActivatedTypes().removeIf(activatedType -> !newTypeIds.contains(activatedType.getId()));

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

            if (policy.getActivatedTypes().stream().noneMatch(ac -> ac.equals(activatedType))) {
                policy.getActivatedTypes().add(activatedType);
            }

        }

        return leavePolicyRepository.merge(policy);
    }

    @Transactional
    public void deleteLeavePolicy(Long organizationId, Long id) {
        leavePolicyRepository.updateStatus(organizationId, id, LeavePolicyStatus.ARCHIVED);
    }

    public LeavePolicy getLeavePolicy(Long organizationId, Long id) throws LeavePolicyNotFoundException {
        return leavePolicyRepository.findByOrganizationIdAndId(organizationId, id)
                .orElseThrow(() -> new LeavePolicyNotFoundException("Leave policy not found with id: " + id));
    }
}

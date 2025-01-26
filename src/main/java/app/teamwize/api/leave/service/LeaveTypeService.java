package app.teamwize.api.leave.service;

import app.teamwize.api.base.domain.entity.EntityStatus;
import app.teamwize.api.leave.exception.LeaveTypeNotFoundException;
import app.teamwize.api.leave.model.command.LeaveTypeCommand;
import app.teamwize.api.leave.model.command.LeaveTypeUpdateCommand;
import app.teamwize.api.leave.model.entity.LeaveType;
import app.teamwize.api.leave.repository.LeaveTypeRepository;
import app.teamwize.api.organization.domain.entity.Organization;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final OrganizationService organizationService;

    public LeaveType getLeaveType(Long organizationId, Long id) throws LeaveTypeNotFoundException {
        return leaveTypeRepository.findByOrganizationIdAndId(organizationId, id)
                .orElseThrow(() -> new LeaveTypeNotFoundException("Leave type not found with id: " + id));
    }

    @Transactional
    public LeaveType createLeaveType(Long organizationId, LeaveTypeCommand newLeaveType) throws OrganizationNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        var leaveType = buildLeaveType(organization, newLeaveType);
        return leaveTypeRepository.persist(leaveType);
    }

    @Transactional
    public List<LeaveType> createLeaveTypes(Long organizationId, List<LeaveTypeCommand> newLeaveTypes) throws OrganizationNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        var types = newLeaveTypes.stream().map(newLeaveType -> buildLeaveType(organization, newLeaveType)).toList();
        return leaveTypeRepository.persistAll(types);
    }

    @Transactional
    public LeaveType updateLeaveType(Long organizationId, Long id, LeaveTypeUpdateCommand request) throws LeaveTypeNotFoundException {
        var existingLeaveType = getLeaveType(organizationId, id)
                .setRequiresApproval(request.requiresApproval())
                .setAmount(request.amount())
                .setSymbol(request.symbol())
                .setName(request.name())
                .setCycle(request.cycle());
        return leaveTypeRepository.merge(existingLeaveType);
    }

    @Transactional
    public void archiveLeaveType(Long organizationId, Long id) {
        leaveTypeRepository.updateStatus(organizationId, id, EntityStatus.ARCHIVED);
    }

    public List<LeaveType> getLeaveTypes(Long organizationId) {
        return leaveTypeRepository.findByOrganizationId(organizationId);
    }

    public boolean isExists(Long id) {
        return leaveTypeRepository.existsById(id);
    }

    private LeaveType buildLeaveType(Organization organization, LeaveTypeCommand newLeaveType) {
        return new LeaveType()
                .setSymbol(newLeaveType.symbol())
                .setRequiresApproval(newLeaveType.requiresApproval())
                .setAmount(newLeaveType.amount())
                .setName(newLeaveType.name())
                .setStatus(EntityStatus.ACTIVE)
                .setCycle(newLeaveType.cycle())
                .setOrganization(organization);
    }
}

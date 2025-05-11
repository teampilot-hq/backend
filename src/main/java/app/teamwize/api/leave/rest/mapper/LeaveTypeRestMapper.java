package app.teamwize.api.leave.rest.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.leave.model.command.LeaveTypeCommand;
import app.teamwize.api.leave.model.command.LeaveTypeUpdateCommand;
import app.teamwize.api.leave.model.entity.LeavePolicyActivatedType;
import app.teamwize.api.leave.model.entity.LeaveType;
import app.teamwize.api.leave.rest.model.request.LeaveTypeCreateRequest;
import app.teamwize.api.leave.rest.model.request.LeaveTypeUpdateRequest;
import app.teamwize.api.leave.rest.model.response.LeavePolicyActivatedTypeResponse;
import app.teamwize.api.leave.rest.model.response.LeaveTypeResponse;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMapperConfig.class)
public interface LeaveTypeRestMapper {

    LeaveTypeResponse toResponse(LeaveType leaveType);

    default LeavePolicyActivatedTypeResponse toActivatedTypeResponse(LeavePolicyActivatedType leaveType) {
        return new LeavePolicyActivatedTypeResponse(
                leaveType.getId().getPolicyId(),
                leaveType.getId().getTypeId(),
                leaveType.getType().getName(),
                leaveType.getType().getSymbol(),
                leaveType.getType().getCycle(),
                leaveType.getAmount(),
                leaveType.getRequiresApproval(),
                leaveType.getStatus()
        );
    }

    LeaveTypeCommand toNewLeaveType(LeaveTypeCreateRequest request);

    LeaveTypeUpdateCommand toUpdateCommand(LeaveTypeUpdateRequest request);
}

package app.teamwize.api.leave.rest.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.leave.model.UserLeaveBalance;
import app.teamwize.api.leave.model.command.LeavePolicyCommand;
import app.teamwize.api.leave.model.entity.LeavePolicy;
import app.teamwize.api.leave.model.entity.LeavePolicyActivatedType;
import app.teamwize.api.leave.rest.model.request.LeavePolicyCreateRequest;
import app.teamwize.api.leave.rest.model.request.LeavePolicyUpdateRequest;
import app.teamwize.api.leave.rest.model.response.LeavePolicyActivatedTypeResponse;
import app.teamwize.api.leave.rest.model.response.LeavePolicyResponse;
import app.teamwize.api.leave.rest.model.response.UserLeaveBalanceResponse;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMapperConfig.class, uses = {LeaveTypeMapper.class})
public interface LeavePolicyMapper {

    LeavePolicyResponse toResponse(LeavePolicy leaveType);

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

    UserLeaveBalanceResponse toResponse(UserLeaveBalance leaveType);

    LeavePolicyCommand toNewLeavePolicy(LeavePolicyCreateRequest request);

    LeavePolicyCommand toUpdateLeavePolicy(LeavePolicyUpdateRequest request);
}

package app.teamwize.api.leave.rest.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.leave.model.UserLeaveBalance;
import app.teamwize.api.leave.model.command.LeavePolicyCommand;
import app.teamwize.api.leave.model.entity.LeavePolicy;
import app.teamwize.api.leave.rest.model.request.LeavePolicyCreateRequest;
import app.teamwize.api.leave.rest.model.request.LeavePolicyUpdateRequest;
import app.teamwize.api.leave.rest.model.response.LeavePolicyResponse;
import app.teamwize.api.leave.rest.model.response.UserLeaveBalanceResponse;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMapperConfig.class, uses = {LeaveTypeMapper.class})
public interface LeavePolicyMapper {

    LeavePolicyResponse toResponse(LeavePolicy leaveType);

    UserLeaveBalanceResponse toResponse(UserLeaveBalance leaveType);

    LeavePolicyCommand toNewLeavePolicy(LeavePolicyCreateRequest request);

    LeavePolicyCommand toUpdateLeavePolicy(LeavePolicyUpdateRequest request);
}

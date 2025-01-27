package app.teamwize.api.leave.rest.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.leave.model.UserLeaveBalance;
import app.teamwize.api.leave.model.command.LeaveCreateCommand;
import app.teamwize.api.leave.model.command.LeaveUpdateCommand;
import app.teamwize.api.leave.model.entity.Leave;
import app.teamwize.api.leave.rest.model.request.LeaveCreateRequest;
import app.teamwize.api.leave.rest.model.request.LeaveUpdateRequest;
import app.teamwize.api.leave.rest.model.response.LeaveResponse;
import app.teamwize.api.leave.rest.model.response.UserLeaveBalanceResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = DefaultMapperConfig.class, uses = {LeaveTypeMapper.class})
public interface LeaveMapper {

    LeaveResponse toDayOffResponse(Leave leave);

    List<LeaveResponse> toDayOffResponseList(List<Leave> leaves);

    UserLeaveBalanceResponse toResponse(UserLeaveBalance balance);

    LeaveCreateCommand toCreateCommand(LeaveCreateRequest request);

    LeaveUpdateCommand toUpdateCommand(LeaveUpdateRequest request);
}

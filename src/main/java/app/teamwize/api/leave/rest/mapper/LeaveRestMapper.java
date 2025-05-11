package app.teamwize.api.leave.rest.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.leave.model.LeaveBalance;
import app.teamwize.api.leave.model.LeaveCheckResult;
import app.teamwize.api.leave.model.command.LeaveCheckCommand;
import app.teamwize.api.leave.model.command.LeaveCreateCommand;
import app.teamwize.api.leave.model.command.LeaveUpdateCommand;
import app.teamwize.api.leave.model.command.LeaveVoteCommand;
import app.teamwize.api.leave.model.entity.Leave;
import app.teamwize.api.leave.rest.model.request.LeaveCheckRequest;
import app.teamwize.api.leave.rest.model.request.LeaveCreateRequest;
import app.teamwize.api.leave.rest.model.request.LeaveUpdateRequest;
import app.teamwize.api.leave.rest.model.request.LeaveVoteRequest;
import app.teamwize.api.leave.rest.model.response.LeaveCheckResponse;
import app.teamwize.api.leave.rest.model.response.LeaveResponse;
import app.teamwize.api.leave.rest.model.response.UserLeaveBalanceResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = DefaultMapperConfig.class, uses = {LeaveTypeRestMapper.class})
public interface LeaveRestMapper {

    LeaveResponse toLeaveResponse(Leave leave);

    List<LeaveResponse> toLeaveResponses(List<Leave> leaves);

    UserLeaveBalanceResponse toUserLeaveBalanceResponse(LeaveBalance balance);

    LeaveCreateCommand toCreateCommand(LeaveCreateRequest request);

    LeaveUpdateCommand toUpdateCommand(LeaveUpdateRequest request);

    LeaveCheckResponse toLeaveCheckResponse(LeaveCheckResult result);

    LeaveCheckCommand toCheckCommand(LeaveCheckRequest request);

    LeaveVoteCommand toVoteCommand(LeaveVoteRequest request);
}

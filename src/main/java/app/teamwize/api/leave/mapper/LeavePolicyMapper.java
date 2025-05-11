package app.teamwize.api.leave.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.leave.model.command.LeaveApproverSaveCommand;
import app.teamwize.api.leave.model.entity.LeavePolicyApprover;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = DefaultMapperConfig.class)
public interface LeavePolicyMapper {

    LeavePolicyApprover toLeaveApprover(LeaveApproverSaveCommand request);

    List<LeavePolicyApprover> toLeaveApprovers(List<LeaveApproverSaveCommand> request);

}

package app.teamwize.api.leave.model.command;

import app.teamwize.api.leave.model.LeaveTypeCycle;

public record LeaveTypeUpdateCommand(
        String symbol,
        String name,
        LeaveTypeCycle cycle,
        Integer amount,
        Boolean requiresApproval) {
}

package app.teamwize.api.leave.rest.model.request;

import app.teamwize.api.leave.model.LeaveTypeCycle;

public record LeaveTypeUpdateRequest(
        String symbol,
        String name,
        LeaveTypeCycle cycle,
        Integer amount,
        Boolean requiresApproval) {
}

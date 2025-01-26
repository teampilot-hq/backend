package app.teamwize.api.leave.rest.model.response;

import app.teamwize.api.base.domain.entity.EntityStatus;
import app.teamwize.api.leave.model.LeaveTypeCycle;

public record LeaveTypeResponse(
        Long id,
        String symbol,
        String name,
        LeaveTypeCycle cycle,
        Integer amount,
        Boolean requiresApproval,
        EntityStatus status) {
}

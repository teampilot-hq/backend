package app.teamwize.api.leave.rest.model.response;

import app.teamwize.api.base.domain.entity.EntityStatus;
import app.teamwize.api.leave.model.LeaveTypeCycle;

public record LeavePolicyActivatedTypeResponse(
        Long policyId,
        Long typeId,
        String name,
        String symbol,
        LeaveTypeCycle cycle,
        Integer amount,
        Boolean requiresApproval,
        EntityStatus status) {
}

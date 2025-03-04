package app.teamwize.api.leave.model.event;

import app.teamwize.api.leave.model.entity.LeavePolicyActivatedType;

public record LeavePolicyActivatedTypePayload(
        String name,
        Boolean requiresApproval,
        Integer amount,
        Long typeId,
        Long policyId,
        String policyName) {

    public LeavePolicyActivatedTypePayload(LeavePolicyActivatedType leavePolicyActivatedType) {
        this(leavePolicyActivatedType.getType().getName(),
                leavePolicyActivatedType.getRequiresApproval(),
                leavePolicyActivatedType.getAmount(),
                leavePolicyActivatedType.getId().getTypeId(),
                leavePolicyActivatedType.getId().getPolicyId(),
                leavePolicyActivatedType.getPolicy().getName()
        );
    }

}
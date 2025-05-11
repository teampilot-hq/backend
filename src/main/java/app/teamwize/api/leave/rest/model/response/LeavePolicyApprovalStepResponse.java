package app.teamwize.api.leave.rest.model.response;

import app.teamwize.api.leave.model.LeaveApproverCondition;


public record LeavePolicyApprovalStepResponse(
        Integer step,
        LeaveApproverCondition condition
) {
}
package app.teamwize.api.leave.rest.model.request;

import app.teamwize.api.leave.model.LeaveApproverCondition;

public record LeavePolicyApprovalStepRequest(
        Integer step,
        LeaveApproverCondition condition) {
}

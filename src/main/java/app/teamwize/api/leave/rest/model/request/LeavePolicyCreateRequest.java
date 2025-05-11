package app.teamwize.api.leave.rest.model.request;


import app.teamwize.api.leave.model.LeavePolicyStatus;

import java.util.List;

public record LeavePolicyCreateRequest(
        String name,
        LeavePolicyStatus status,
        List<LeaveApproverRequest> approvers,
        List<LeavePolicyApprovalStepRequest> approvalSteps,
        List<LeavePolicyActivatedTypeRequest> activatedTypes) {
}


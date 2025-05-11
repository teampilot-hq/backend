package app.teamwize.api.leave.model.command;

import app.teamwize.api.leave.model.LeaveApproverCondition;
import app.teamwize.api.leave.model.LeavePolicyStatus;

import java.util.List;

public record LeavePolicyCommand(
        String name,
        LeavePolicyStatus status,
        List<LeavePolicyApprovalStep> approvalSteps,
        List<LeaveApproverSaveCommand> approvers,
        List<LeavePolicyActivatedTypeCommand> activatedTypes) {
    public record LeavePolicyApprovalStep(Integer step, LeaveApproverCondition condition) {
    }
}


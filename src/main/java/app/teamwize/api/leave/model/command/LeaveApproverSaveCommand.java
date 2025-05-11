package app.teamwize.api.leave.model.command;


public record LeaveApproverSaveCommand(
        Integer step,
        Long userId) {
}
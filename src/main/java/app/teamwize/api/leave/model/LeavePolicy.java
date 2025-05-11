package app.teamwize.api.leave.model;

public record LeavePolicy(
        Long id,
        String name,
        LeavePolicyStatus status) {
}

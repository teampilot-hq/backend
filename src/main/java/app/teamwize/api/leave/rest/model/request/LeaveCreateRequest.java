package app.teamwize.api.leave.rest.model.request;


import java.time.LocalDateTime;

public record LeaveCreateRequest(
        Long typeId,
        Long policyId,
        String reason,
        LocalDateTime start,
        LocalDateTime end
) {
}

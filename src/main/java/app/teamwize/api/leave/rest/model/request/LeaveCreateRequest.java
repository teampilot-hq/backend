package app.teamwize.api.leave.rest.model.request;


import java.time.Instant;

public record LeaveCreateRequest(
        Long typeId,
        Long policyId,
        String reason,
        Instant start,
        Instant end) {
}

package app.teamwize.api.leave.rest.model.request;

import java.time.Instant;

public record LeaveCheckRequest(Long typeId, Instant start, Instant end) {
}

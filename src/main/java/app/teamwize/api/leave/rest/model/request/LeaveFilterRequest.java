package app.teamwize.api.leave.rest.model.request;

import app.teamwize.api.leave.model.LeaveStatus;

import java.time.Instant;

public record LeaveFilterRequest(Long teamId, Long userId, LeaveStatus status, Instant start, Instant end) {
}

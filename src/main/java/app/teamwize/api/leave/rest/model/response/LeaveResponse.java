package app.teamwize.api.leave.rest.model.response;

import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.user.domain.response.UserCompactResponse;

import java.time.Instant;

public record LeaveResponse(
        Long id,
        Instant createdAt,
        Instant updatedAt,
        Instant startAt,
        Instant endAt,
        LeaveStatus status,
        Float duration,
        LeavePolicyActivatedTypeResponse activatedType,
        String reason,
        UserCompactResponse user) {
}

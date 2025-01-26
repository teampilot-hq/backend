package app.teamwize.api.leave.rest.model.response;

import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.user.domain.response.UserCompactResponse;

import java.time.LocalDateTime;

public record LeaveResponse(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LeaveStatus status,
        Float duration,
        LeavePolicyActivatedTypeResponse activatedType,
        LeavePolicyCompactResponse policy,
        String reason,
        UserCompactResponse user) {
}

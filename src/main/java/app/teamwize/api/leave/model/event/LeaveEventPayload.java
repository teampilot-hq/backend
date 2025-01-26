package app.teamwize.api.leave.model.event;

import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.leave.model.entity.Leave;
import app.teamwize.api.leave.model.entity.LeavePolicyActivatedType;

import java.time.LocalDateTime;

public record LeaveEventPayload(
        Long id,
        LocalDateTime startAt,

        LocalDateTime endAt,

        LeaveStatus status,

        LeavePolicyActivatedType type,

        String reason,

        Float duration) {

    public LeaveEventPayload(Leave leave) {
        this(
                leave.getId(),
                leave.getStartAt(),
                leave.getEndAt(),
                leave.getStatus(),
                leave.getActivatedType(),
                leave.getReason(),
                leave.getDuration()
        );
    }

}
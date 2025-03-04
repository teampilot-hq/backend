package app.teamwize.api.leave.model.event;

import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.leave.model.entity.Leave;

import java.time.Instant;

public record LeaveEventPayload(
        Long id,
        Instant startAt,

        Instant endAt,

        LeaveStatus status,

        LeavePolicyActivatedTypePayload type,

        String reason,

        Float duration) {

    public LeaveEventPayload(Leave leave) {
        this(
                leave.getId(),
                leave.getStartAt(),
                leave.getEndAt(),
                leave.getStatus(),
                new LeavePolicyActivatedTypePayload(leave.getActivatedType()),
                leave.getReason(),
                leave.getDuration()
        );
    }

}
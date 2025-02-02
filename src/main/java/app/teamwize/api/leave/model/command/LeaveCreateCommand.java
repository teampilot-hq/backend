package app.teamwize.api.leave.model.command;

import java.time.Instant;

public record LeaveCreateCommand(
        Long typeId,
        String reason,
        Instant start,
        Instant end) {
}

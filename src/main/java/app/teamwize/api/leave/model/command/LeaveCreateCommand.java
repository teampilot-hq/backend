package app.teamwize.api.leave.model.command;

import java.time.LocalDateTime;

public record LeaveCreateCommand(
        Long typeId,
        String reason,
        LocalDateTime start,
        LocalDateTime end) {
}

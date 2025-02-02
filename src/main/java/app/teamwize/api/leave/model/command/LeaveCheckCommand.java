package app.teamwize.api.leave.model.command;

import java.time.Instant;

public record LeaveCheckCommand(Long typeId, Instant start, Instant end) {
}

package app.teamwize.api.event.model;


import java.time.Instant;
import java.util.Map;


public record EventExecution(
        Long id,
        Instant createdAt,
        Instant updatedAt,
        EventExecutionStatus status,
        EventExitCode exitCode,
        Integer attempts,
        String handler,
        Map<String, Object> metadata) {
}

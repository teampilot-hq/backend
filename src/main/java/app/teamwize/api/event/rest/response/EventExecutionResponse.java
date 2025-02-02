package app.teamwize.api.event.rest.response;


import app.teamwize.api.event.model.EventExecutionStatus;
import app.teamwize.api.event.model.EventExitCode;

import java.time.Instant;
import java.util.Map;


public record EventExecutionResponse(
        Long id,
        Instant createdAt,
        Instant updatedAt,
        EventExecutionStatus status,
        EventExitCode exitCode,
        Integer attempts,
        String handler,
        Map<String, Object> metadata) {
}

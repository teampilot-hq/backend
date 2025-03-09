package app.teamwize.api.event.service.handler;

import app.teamwize.api.event.model.Event;
import app.teamwize.api.event.model.EventExitCode;
import app.teamwize.api.event.model.EventType;

import java.util.Map;

public interface EventHandler {
    String name();

    boolean accepts(EventType type);

    EventExecutionResult process(Event eventEntity);

    record EventExecutionResult(EventExitCode exitCode, Map<String, Object> metadata) {
    }

}

package app.teamwize.api.leave.model.event;

import app.teamwize.api.auth.domain.event.UserEventPayload;
import app.teamwize.api.event.model.EventPayload;
import app.teamwize.api.event.model.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "LEAVE_STATUS_UPDATED", description = "Leave status updated event")
public record LeaveStatusUpdatedEvent(LeaveEventPayload leave, UserEventPayload user) implements EventPayload {

    @Override
    public EventType name() {
        return EventType.LEAVE_STATUS_UPDATED;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "leave", leave,
                "user", user
        );
    }


}

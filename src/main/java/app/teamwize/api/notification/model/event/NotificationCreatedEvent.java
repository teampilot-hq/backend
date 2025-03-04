package app.teamwize.api.notification.model.event;

import app.teamwize.api.event.model.EventPayload;
import app.teamwize.api.event.model.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "NOTIFICATION_CREATED", description = "Notification created event")
public record NotificationCreatedEvent(NotificationEventPayload notification) implements EventPayload {

    @Override
    public EventType name() {
        return EventType.NOTIFICATION_CREATED;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "notification", notification
        );
    }


}

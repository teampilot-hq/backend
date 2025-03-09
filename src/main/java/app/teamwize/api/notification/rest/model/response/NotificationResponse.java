package app.teamwize.api.notification.rest.model.response;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record NotificationResponse(
        Long id,
        String title,
        NotificationTriggerCompactResponse trigger,
        String textContent,
        String htmlContent,
        EventType eventType,
        Map<String, Object> params,
        List<NotificationChannel> channels,
        Instant sentAt,
        NotificationStatus status
) {
}

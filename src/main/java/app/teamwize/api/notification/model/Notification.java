package app.teamwize.api.notification.model;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.organization.domain.entity.Organization;
import app.teamwize.api.user.domain.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record Notification(
        Long id,
        String title,
        User user,
        NotificationTrigger trigger,
        String textContent,
        String htmlContent,
        EventType eventType,
        Organization organization,
        Map<String, Object> params,
        List<NotificationChannel> channels,
        Instant sentAt,
        NotificationStatus status
) {
}

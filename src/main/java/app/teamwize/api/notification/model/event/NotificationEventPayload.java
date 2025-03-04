package app.teamwize.api.notification.model.event;

import app.teamwize.api.auth.domain.event.OrganizationEventPayload;
import app.teamwize.api.auth.domain.event.UserEventPayload;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.Notification;
import app.teamwize.api.notification.model.NotificationChannel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record NotificationEventPayload(
        Long id,
        String title,
        UserEventPayload user,
        NotificationTriggerEventPayload trigger,
        String textContent,
        String htmlContent,
        EventType eventType,
        OrganizationEventPayload organization,
        Map<String, Object> params,
        List<NotificationChannel> channels,
        Instant sentAt) {

    public NotificationEventPayload(Notification notification) {
        this(
                notification.id(),
                notification.title(),
                new UserEventPayload(notification.user()),
                new NotificationTriggerEventPayload(notification.trigger()),
                notification.textContent(),
                notification.htmlContent(),
                notification.eventType(),
                new OrganizationEventPayload(notification.organization()),
                notification.params(),
                notification.channels(),
                notification.sentAt()
        );
    }

}

package app.teamwize.api.notification.rest.model.response;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationTriggerReceptors;
import app.teamwize.api.notification.model.NotificationTriggerStatus;

import java.util.List;

public record NotificationTriggerResponse(
        Long id,
        String title,
        String name,
        String textTemplate,
        String htmlTemplate,
        EventType eventType,
        List<NotificationChannel> channels,
        NotificationTriggerReceptors receptors,
        NotificationTriggerStatus status) {
}

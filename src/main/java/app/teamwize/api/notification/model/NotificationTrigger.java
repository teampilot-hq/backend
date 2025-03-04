package app.teamwize.api.notification.model;

import app.teamwize.api.event.model.EventType;

import java.util.List;

public record NotificationTrigger(
        Long id,
        String name,
        String title,
        String textTemplate,
        String htmlTemplate,
        EventType eventType,
        List<NotificationChannel> channels,
        NotificationTriggerReceptors receptors,
        NotificationTriggerStatus status) {
}

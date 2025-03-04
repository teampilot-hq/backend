package app.teamwize.api.notification.model.command;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationTriggerReceptors;

import java.util.List;

public record NotificationTriggerUpdateCommand(
        String name,
        String title,
        String textTemplate,
        String htmlTemplate,
        EventType eventType,
        List<NotificationChannel> channels,
        NotificationTriggerReceptors receptors) {
}

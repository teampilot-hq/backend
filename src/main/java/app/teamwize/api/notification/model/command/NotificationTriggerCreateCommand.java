package app.teamwize.api.notification.model.command;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;

import java.util.List;

public record NotificationTriggerCreateCommand(
        String name,
        String title,
        String textTemplate,
        String htmlTemplate,
        EventType eventType,
        List<NotificationChannel> channels,
        String receptors) {
}

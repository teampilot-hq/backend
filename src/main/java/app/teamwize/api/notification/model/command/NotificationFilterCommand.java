package app.teamwize.api.notification.model.command;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationStatus;

public record NotificationFilterCommand(NotificationStatus status, EventType eventType) {
}

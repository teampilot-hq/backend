package app.teamwize.api.notification.rest.model.request;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationStatus;

public record NotificationFilterRequest(NotificationStatus status, EventType eventType, Long userId) {
}

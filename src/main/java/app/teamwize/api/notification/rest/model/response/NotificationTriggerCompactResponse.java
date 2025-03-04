package app.teamwize.api.notification.rest.model.response;

import app.teamwize.api.notification.model.NotificationTriggerReceptors;
import app.teamwize.api.notification.model.NotificationTriggerStatus;

public record NotificationTriggerCompactResponse(
        Long id,
        String title,
        String name,
        NotificationTriggerReceptors receptors,
        NotificationTriggerStatus status) {
}

package app.teamwize.api.notification.model.event;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationTrigger;
import app.teamwize.api.notification.model.NotificationTriggerReceptors;
import app.teamwize.api.notification.model.NotificationTriggerStatus;

import java.util.List;

public record NotificationTriggerEventPayload(
        Long id,
        String title,
        String name,
        String textTemplate,
        String htmlTemplate,
        EventType eventType,
        List<NotificationChannel> channels,
        NotificationTriggerReceptors receptors,
        NotificationTriggerStatus status
) {

    public NotificationTriggerEventPayload(NotificationTrigger trigger) {
        this(trigger.id(), trigger.name(), trigger.title(), trigger.textTemplate(), trigger.htmlTemplate(), trigger.eventType(),
                trigger.channels(), trigger.receptors(), trigger.status());
    }
}

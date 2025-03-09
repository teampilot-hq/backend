package app.teamwize.api.notification.model.command;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.user.domain.entity.User;

import java.util.Map;

public record NotificationCreateCommand(
        String title,
        Long triggerId,
        Long eventId,
        EventType event,
        User user,
        Map<String, Object> params,
        NotificationChannel channel
) {
}

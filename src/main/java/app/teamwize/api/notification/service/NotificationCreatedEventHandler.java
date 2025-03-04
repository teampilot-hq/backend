package app.teamwize.api.notification.service;

import app.teamwize.api.event.entity.EventEntity;
import app.teamwize.api.event.model.EventExitCode;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.event.service.handler.EventHandler;
import app.teamwize.api.notification.exception.NotificationSendFailureException;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.event.NotificationCreatedEvent;
import app.teamwize.api.notification.service.notifier.Notifier;
import app.teamwize.api.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationCreatedEventHandler implements EventHandler {

    private final ObjectMapper objectMapper;
    private final List<Notifier> notifiers;
    private final UserService userService;

    @Override
    public String name() {
        return "NotificationSender";
    }

    @Override
    public boolean accepts(EventType type) {
        return type == EventType.NOTIFICATION_CREATED;
    }

    @Override
    public EventExecutionResult process(EventEntity eventEntity) {
        try {
            var notificationPayload = objectMapper.writeValueAsString(eventEntity.getParams());
            var event = objectMapper.readValue(notificationPayload, NotificationCreatedEvent.class);

            log.info("Notification payload: {}", event);
            for (var notifier : notifiers) {
                for (NotificationChannel channel : event.notification().channels()) {
                    if (notifier.accepts(channel)) {
                        notifier.notify(event.notification());
                    }
                }
            }
            return new EventExecutionResult(EventExitCode.SUCCESS, null);
        } catch (JsonProcessingException | NotificationSendFailureException e) {
            return new EventExecutionResult(EventExitCode.ERROR, null);
        }
    }
}
package app.teamwize.api.notification.service;

import app.teamwize.api.auth.domain.event.OrganizationCreatedEvent;
import app.teamwize.api.event.entity.EventEntity;
import app.teamwize.api.event.model.EventExitCode;
import app.teamwize.api.event.model.EventPayload;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.event.service.handler.EventHandler;
import app.teamwize.api.leave.model.event.LeaveCreatedEvent;
import app.teamwize.api.leave.model.event.LeaveStatusUpdatedEvent;
import app.teamwize.api.notification.model.Notification;
import app.teamwize.api.notification.model.NotificationTrigger;
import app.teamwize.api.notification.model.command.NotificationCreateCommand;
import app.teamwize.api.notification.model.event.NotificationCreatedEvent;
import app.teamwize.api.user.domain.UserRole;
import app.teamwize.api.user.domain.entity.User;
import app.teamwize.api.user.domain.event.UserInvitedEvent;
import app.teamwize.api.user.exception.UserNotFoundException;
import app.teamwize.api.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventHandler implements EventHandler {

    private final NotificationTriggerService notificationTriggerService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean accepts(EventType type) {
        return type != EventType.NOTIFICATION_CREATED;
    }

    @Override
    public EventExecutionResult process(EventEntity eventEntity) {
        var triggers = notificationTriggerService.getNotificationTriggers(
                eventEntity.getOrganization().getId(),
                eventEntity.getType()
        );
        var notifications = new ArrayList<Notification>();
        try {
            for (var trigger : triggers) {
                // Then compile with handlebars for any dynamic values
                var receptors = compileReceptors(eventEntity, trigger);
                log.info("Processing notification for receptors: {} , event : {} , trigger : {}", receptors, eventEntity.getType(), trigger.id());
                for (var receptor : receptors) {
                    // try to compile the template
                    var notification = notificationService.createNotification(eventEntity.getOrganization().getId(),
                            new NotificationCreateCommand(
                                    trigger.title(),
                                    trigger.id(),
                                    eventEntity.getId(),
                                    eventEntity.getType(),
                                    receptor,
                                    eventEntity.getParams(),
                                    trigger.channels().getFirst())
                    );
                    notifications.add(notification);
                    log.info("Notification created: {}", notification);
                }
            }
            return new EventExecutionResult(EventExitCode.SUCCESS,
                    Map.of("triggers", triggers.size(), "notifications", notifications.size())
            );
        } catch (Exception e) {
            log.error("Error while processing notification", e);
            return new EventExecutionResult(EventExitCode.ERROR, Map.of("error", e.getMessage()));
        }
    }

    private <T extends EventPayload> T getEventPayload(EventEntity event, Class<T> clazz) throws IOException {
        var notificationPayload = objectMapper.writeValueAsString(event.getParams());
        return objectMapper.readValue(notificationPayload, clazz);
    }

    private List<User> compileReceptors(EventEntity event, NotificationTrigger trigger) throws IOException, UserNotFoundException {
        if (trigger.receptors() == null) {
            return List.of();
        }
        var organizationId = event.getOrganization().getId();
        Long teamId;
        Long userId;
        switch (event.getType()) {
            case USER_CREATED -> {
                var payload = getEventPayload(event, UserInvitedEvent.class);
                userId = payload.user().id();
                teamId = payload.user().teamId();
            }
            case LEAVE_CREATED -> {
                var payload = getEventPayload(event, LeaveCreatedEvent.class);
                teamId = payload.user().teamId();
                userId = payload.user().id();
            }
            case LEAVE_STATUS_UPDATED -> {
                var payload = getEventPayload(event, LeaveStatusUpdatedEvent.class);

                teamId = payload.user().teamId();
                userId = payload.user().id();
            }
            case NOTIFICATION_CREATED -> {
                var payload = getEventPayload(event, NotificationCreatedEvent.class);
                userId = payload.notification().user().id();
                teamId = payload.notification().user().teamId();
            }
            case ORGANIZATION_CREATED -> {
                var payload = getEventPayload(event, OrganizationCreatedEvent.class);
                userId = payload.user().id();
                teamId = payload.user().teamId();
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + event.getType());
            }
        }

        switch (trigger.receptors()) {
            case USER -> {
                var user = userService.getUser(organizationId, userId);
                return List.of(user);
            }
            case ALL_TEAM_MEMBERS -> {
                return userService.getUsersByTeam(organizationId, teamId, List.of(UserRole.EMPLOYEE, UserRole.TEAM_ADMIN));
            }
            case ORGANIZATION_ADMIN -> {
                return userService.getUsersByRole(event.getOrganization().getId(), UserRole.ORGANIZATION_ADMIN);
            }
            case TEAM_ADMIN -> {
                return userService.getUsersByRole(event.getOrganization().getId(), UserRole.TEAM_ADMIN);
            }
        }
        return Collections.emptyList();
    }
}

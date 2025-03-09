package app.teamwize.api.notification.listener;

import app.teamwize.api.auth.domain.event.OrganizationCreatedEvent;
import app.teamwize.api.event.model.Event;
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
import app.teamwize.api.notification.service.NotificationService;
import app.teamwize.api.notification.service.NotificationTriggerService;
import app.teamwize.api.user.domain.UserRole;
import app.teamwize.api.user.domain.entity.User;
import app.teamwize.api.user.domain.event.UserInvitedEvent;
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

/**
 * Handles events to create user notifications based on configured triggers.
 */
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
    public EventExecutionResult process(Event event) {
        try {
            long organizationId = event.organization().getId();
            List<NotificationTrigger> triggers = notificationTriggerService.getNotificationTriggers(
                    organizationId,
                    event.type()
            );

            if (triggers.isEmpty()) {
                log.info("No triggers found for event type: {}", event.type());
                return new EventExecutionResult(EventExitCode.SUCCESS, Map.of("triggers", 0, "notifications", 0));
            }

            int notificationCount = createNotificationsForTriggers(event, triggers);
            return new EventExecutionResult(
                    EventExitCode.SUCCESS,
                    Map.of("triggers", triggers.size(), "notifications", notificationCount)
            );
        } catch (Exception e) {
            log.error("Error while processing notification for event {}: {}", event.id(), e.getMessage(), e);
            return new EventExecutionResult(EventExitCode.ERROR, Map.of("error", e.getMessage()));
        }
    }

    /**
     * Creates notifications for all applicable triggers.
     *
     * @param event The event that triggered notifications
     * @param triggers The notification triggers to process
     * @return Count of notifications created
     */
    private int createNotificationsForTriggers(Event event, List<NotificationTrigger> triggers) {
        List<Notification> notifications = new ArrayList<>();

        for (NotificationTrigger trigger : triggers) {
            try {
                List<User> receptors = getReceptorsForTrigger(event, trigger);
                if (receptors.isEmpty()) {
                    log.info("No receptors found for event: {} and trigger: {}", event.type(), trigger.id());
                    continue;
                }

                log.debug("Processing notification for {} receptors, event: {}, trigger: {}",
                        receptors.size(), event.type(), trigger.id());

                createNotificationsForReceptors(event, trigger, receptors, notifications);
            } catch (Exception e) {
                log.error("Failed to process trigger {}: {}", trigger.id(), e.getMessage(), e);
                // Continue processing other triggers instead of failing the entire process
            }
        }

        return notifications.size();
    }

    /**
     * Creates notifications for each receptor in the list.
     *
     * @param eventEntity The event that triggered notifications
     * @param trigger The notification trigger configuration
     * @param receptors List of users who should receive notifications
     * @param notifications Collection to which created notifications are added
     */
    private void createNotificationsForReceptors(
            Event eventEntity,
            NotificationTrigger trigger,
            List<User> receptors,
            List<Notification> notifications) {

        long organizationId = eventEntity.organization().getId();

        for (User receptor : receptors) {
            try {
                NotificationCreateCommand command = new NotificationCreateCommand(
                        trigger.title(),
                        trigger.id(),
                        eventEntity.id(),
                        eventEntity.type(),
                        receptor,
                        eventEntity.params(),
                        trigger.channels().getFirst()
                );

                Notification notification = notificationService.createNotification(organizationId, command);
                notifications.add(notification);
                log.debug("Notification created for user {}, trigger {}", receptor.getId(), trigger.id());
            } catch (Exception e) {
                log.error("Failed to create notification for user {}: {}", receptor.getId(), e.getMessage());
                // Continue with other receptors
            }
        }
    }

    /**
     * Gets the users who should receive the notification for a specific trigger.
     *
     * @param event The event that triggered the notification
     * @param trigger The notification trigger configuration
     * @return List of users who should receive the notification
     */
    private List<User> getReceptorsForTrigger(Event event, NotificationTrigger trigger) {
        if (trigger.receptors() == null) {
            return Collections.emptyList();
        }
        try {
            // Extract context information from the event
            Long userId = null;
            Long teamId = null;

            switch (event.type()) {
                case USER_CREATED -> {
                    UserInvitedEvent payload = getEventPayload(event, UserInvitedEvent.class);
                    userId = payload.user().id();
                    teamId = payload.user().teamId();
                }
                case LEAVE_CREATED -> {
                    LeaveCreatedEvent payload = getEventPayload(event, LeaveCreatedEvent.class);
                    teamId = payload.user().teamId();
                    userId = payload.user().id();
                }
                case LEAVE_STATUS_UPDATED -> {
                    LeaveStatusUpdatedEvent payload = getEventPayload(event, LeaveStatusUpdatedEvent.class);
                    teamId = payload.user().teamId();
                    userId = payload.user().id();
                }
                case NOTIFICATION_CREATED -> {
                    NotificationCreatedEvent payload = getEventPayload(event, NotificationCreatedEvent.class);
                    userId = payload.notification().user().id();
                    teamId = payload.notification().user().teamId();
                }
                case ORGANIZATION_CREATED -> {
                    OrganizationCreatedEvent payload = getEventPayload(event, OrganizationCreatedEvent.class);
                    userId = payload.user().id();
                    teamId = payload.user().teamId();
                }
                default -> throw new IllegalStateException("Unsupported event type: " + event.type());
            }

            // Determine recipients based on the receptor type
            long organizationId = event.organization().getId();
            return switch (trigger.receptors()) {
                case USER -> {
                    if (userId == null) {
                        log.warn("User ID not found in event payload for event type: {}", event.type());
                        yield Collections.emptyList();
                    }
                    yield List.of(userService.getUser(organizationId, userId));
                }
                case ALL_TEAM_MEMBERS -> {
                    if (teamId == null) {
                        log.warn("Team ID not found in event payload for event type: {}", event.type());
                        yield Collections.emptyList();
                    }
                    yield userService.getUsersByTeam(
                            organizationId,
                            teamId,
                            List.of(UserRole.EMPLOYEE, UserRole.TEAM_ADMIN)
                    );
                }
                case REVIEWERS -> {
                    if (teamId == null) {
                        log.warn("Team ID not found in event payload for event type: {}", event.type());
                        yield Collections.emptyList();
                    }
                    var reviewers = new ArrayList<User>();
                    var orgAdmins = userService.getUsersByRole(organizationId, UserRole.ORGANIZATION_ADMIN);
                    var teamAdmins = userService.getUsersByTeam(organizationId, teamId, List.of(UserRole.TEAM_ADMIN));
                    reviewers.addAll(orgAdmins);
                    reviewers.addAll(teamAdmins);
                    yield reviewers;
                }
                case ORGANIZATION_ADMIN -> userService.getUsersByRole(organizationId, UserRole.ORGANIZATION_ADMIN);
                case TEAM_ADMIN -> userService.getUsersByTeam(organizationId, teamId, List.of(UserRole.TEAM_ADMIN));
            };
        } catch (Exception e) {
            log.error("Error resolving receptors for event {} and trigger {}: {}",
                    event.id(), trigger.id(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Extracts an event payload from the event entity and deserializes it to the specified class.
     *
     * @param event The event entity
     * @param clazz Target class for deserialization
     * @return Deserialized payload
     * @throws IOException If deserialization fails
     */
    private <T extends EventPayload> T getEventPayload(Event event, Class<T> clazz) throws IOException {
        String payloadJson = objectMapper.writeValueAsString(event.params());
        return objectMapper.readValue(payloadJson, clazz);
    }
}
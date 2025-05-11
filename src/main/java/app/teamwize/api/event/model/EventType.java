package app.teamwize.api.event.model;

import app.teamwize.api.auth.domain.event.OrganizationCreatedEvent;
import app.teamwize.api.leave.model.event.LeaveCreatedEvent;
import app.teamwize.api.leave.model.event.LeaveStatusUpdatedEvent;
import app.teamwize.api.leave.model.event.LeaveVotedEvent;
import app.teamwize.api.notification.model.NotificationTriggerReceptors;
import app.teamwize.api.notification.model.event.NotificationCreatedEvent;
import app.teamwize.api.team.domain.event.TeamCreatedEvent;
import app.teamwize.api.user.domain.event.UserInvitedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static app.teamwize.api.notification.model.NotificationTriggerReceptors.*;

@RequiredArgsConstructor
@Getter
public enum EventType {
    ORGANIZATION_CREATED(OrganizationCreatedEvent.class, List.of(USER, ORGANIZATION_ADMIN)),
    USER_CREATED(UserInvitedEvent.class, List.of(USER, TEAM_ADMIN, ORGANIZATION_ADMIN)),
    LEAVE_CREATED(LeaveCreatedEvent.class, List.of(USER, TEAM_ADMIN, ORGANIZATION_ADMIN)),
    LEAVE_VOTED(LeaveVotedEvent.class, List.of(USER, TEAM_ADMIN, ORGANIZATION_ADMIN)),
    LEAVE_STATUS_UPDATED(LeaveStatusUpdatedEvent.class, List.of(USER, TEAM_ADMIN, ORGANIZATION_ADMIN)),
    TEAM_CREATED(TeamCreatedEvent.class, List.of(USER, TEAM_ADMIN, ORGANIZATION_ADMIN, ALL_TEAM_MEMBERS)),
    NOTIFICATION_CREATED(NotificationCreatedEvent.class, List.of(USER, TEAM_ADMIN, ORGANIZATION_ADMIN, ALL_TEAM_MEMBERS));

    private final Class<? extends EventPayload> payloadType;
    private final List<NotificationTriggerReceptors> supportedReceptors;
}

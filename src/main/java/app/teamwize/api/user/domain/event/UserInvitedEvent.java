package app.teamwize.api.user.domain.event;

import app.teamwize.api.auth.domain.event.OrganizationEventPayload;
import app.teamwize.api.auth.domain.event.UserEventPayload;
import app.teamwize.api.event.model.EventPayload;
import app.teamwize.api.event.model.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "USER_CREATED", description = "User created event")
public record UserInvitedEvent(UserEventPayload user, OrganizationEventPayload organization) implements EventPayload {
    @Override
    public EventType name() {
        return EventType.USER_CREATED;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "user", user,
                "organization", organization
        );
    }
}

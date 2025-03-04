package app.teamwize.api.auth.domain.event;

import app.teamwize.api.event.model.EventPayload;
import app.teamwize.api.event.model.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "ORGANIZATION_CREATED", description = "Organization created event")
public record OrganizationCreatedEvent(UserEventPayload user,
                                       OrganizationEventPayload organization) implements EventPayload {
    @Override
    public EventType name() {
        return EventType.ORGANIZATION_CREATED;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "organization", organization,
                "user", user
        );
    }



}
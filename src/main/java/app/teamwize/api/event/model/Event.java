package app.teamwize.api.event.model;

import app.teamwize.api.organization.domain.entity.Organization;

import java.time.Instant;
import java.util.Map;

public record Event(
        Long id,
        EventType type,
        Organization organization,
        Map<String, Object> params,
        EventStatus status,
        Byte maxAttempts,
        Instant scheduledAt
) {
}

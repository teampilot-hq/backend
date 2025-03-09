package app.teamwize.api.notification.model.response;

import java.util.Map;

public record EventSchemaResponse(
        String event,
        String description,
        Map<String, Object> schema
) {
}
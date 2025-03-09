package app.teamwize.api.notification.service;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationTriggerReceptors;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventSchemaService {

    public List<EventSchema> getEventSchemas() {
        return Arrays.stream(EventType.values())
                .map(this::createEventSchema)
                .toList();
    }

    private EventSchema createEventSchema(EventType eventType) {
        List<FieldSchema> fields = Arrays.stream(eventType.getPayloadType().getDeclaredFields())
                .map(this::createFieldSchema)
                .toList();

        SchemaObject schema = new SchemaObject("object", fields);

        return new EventSchema(
                getEventName(eventType.getPayloadType()),
                getEventDescription(eventType.getPayloadType()),
                schema,
                eventType.getSupportedReceptors()
        );
    }

    private FieldSchema createFieldSchema(Field field) {
        Class<?> type = field.getType();
        String schemaType = getSchemaType(type);
        String name = field.getName();
        boolean required = !field.isAnnotationPresent(Nullable.class);
        String description = getFieldDescription(field);
        List<String> enumValues = null;
        List<FieldSchema> properties = null;
        ItemSchema items = null;

        // Handle nested objects
        if (schemaType.equals("object") && !type.getName().startsWith("java.")) {
            properties = Arrays.stream(type.getDeclaredFields())
                    .map(this::createFieldSchema)
                    .toList();
        }

        // Handle arrays/collections
        if (schemaType.equals("array")) {
            Class<?> componentType = getComponentType(field);
            if (componentType != null) {
                if (componentType.isEnum()) {
                    List<String> itemEnumValues = Arrays.stream(componentType.getEnumConstants())
                            .map(Object::toString)
                            .toList();

                    items = new ItemSchema("enum", itemEnumValues, null);
                } else if (!componentType.getName().startsWith("java.")) {
                    List<FieldSchema> itemProperties = Arrays.stream(componentType.getDeclaredFields())
                            .map(this::createFieldSchema)
                            .toList();

                    items = new ItemSchema("object", null, itemProperties);
                } else {
                    items = new ItemSchema(getSchemaType(componentType), null, null);
                }
            }
        }

        // Handle enums
        if (type.isEnum()) {
            enumValues = Arrays.stream(type.getEnumConstants())
                    .map(Object::toString)
                    .toList();
        }

        return new FieldSchema(name, schemaType, required, description, enumValues, properties, items);
    }

    private Class<?> getComponentType(Field field) {
        Class<?> type = field.getType();
        if (type.isArray()) {
            return type.getComponentType();
        }
        if (Collection.class.isAssignableFrom(type)) {
            var genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType paramType) {
                var actualTypeArg = paramType.getActualTypeArguments()[0];
                if (actualTypeArg instanceof Class) {
                    return (Class<?>) actualTypeArg;
                }
            }
        }
        return null;
    }

    private String getSchemaType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Long.class || type == long.class) return "integer";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        if (type == Double.class || type == double.class) return "number";
        if (type == Float.class || type == float.class) return "number";
        if (type.isEnum()) return "enum";
        if (type.isArray()) return "array";
        if (Collection.class.isAssignableFrom(type)) return "array";
        if (Map.class.isAssignableFrom(type)) return "object";
        return "object";
    }

    private String getEventName(Class<?> eventClass) {
        var schema = eventClass.getAnnotation(Schema.class);
        return schema != null ? schema.name() : eventClass.getSimpleName();
    }

    private String getEventDescription(Class<?> eventClass) {
        var schema = eventClass.getAnnotation(Schema.class);
        return schema != null ? schema.description() : eventClass.getSimpleName();
    }

    private String getFieldDescription(Field field) {
        var schema = field.getAnnotation(Schema.class);
        return schema != null ? schema.description() : "";
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EventSchema(
            String name,
            String description,
            SchemaObject schema,
            List<NotificationTriggerReceptors> receptors) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SchemaObject(
            String type,
            List<FieldSchema> properties
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FieldSchema(
            String name,
            String type,
            boolean required,
            String description,
            List<String> enumValues,
            List<FieldSchema> properties,
            ItemSchema items
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ItemSchema(
            String type,
            List<String> enumValues,
            List<FieldSchema> properties
    ) {
    }
}
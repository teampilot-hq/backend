package app.teamwize.api.notification.repository;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationStatus;
import app.teamwize.api.notification.model.entity.NotificationEntity;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecifications {

    public static Specification<NotificationEntity> withOrganizationId(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("organization").get("id"), organizationId);
    }

    public static Specification<NotificationEntity> withUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<NotificationEntity> withEventType(EventType eventType) {
        if (eventType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), eventType);
    }

    public static Specification<NotificationEntity> withStatus(NotificationStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}

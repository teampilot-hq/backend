package app.teamwize.api.notification.repository;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationTriggerStatus;
import app.teamwize.api.notification.model.entity.NotificationTriggerEntity;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTriggerRepository extends BaseJpaRepository<NotificationTriggerEntity, Long> {
    List<NotificationTriggerEntity> findByOrganizationIdAndEventTypeAndStatus(Long organizationId, EventType eventType, NotificationTriggerStatus status);

    Optional<NotificationTriggerEntity> findByOrganizationIdAndId(Long organizationId, Long id);

    List<NotificationTriggerEntity> findByOrganizationId(Long organizationId);

    Integer deleteByOrganizationIdAndId(Long userOrganizationId, Long id);
}
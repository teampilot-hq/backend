package app.teamwize.api.notification.repository;

import app.teamwize.api.notification.model.NotificationStatus;
import app.teamwize.api.notification.model.entity.NotificationEntity;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends BaseJpaRepository<NotificationEntity, Long>, JpaSpecificationExecutor<NotificationEntity> {

    @EntityGraph(attributePaths = {"trigger", "user"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<NotificationEntity> findAll(Specification<NotificationEntity> spec, Pageable pageable);

    Long countByOrganizationIdAndUserIdAndStatusIsIn(Long organizationId, Long userId, List<NotificationStatus> statuses);

    @Modifying
    @Query("update NotificationEntity n set n.status = :notificationStatus where n.organization.id = :organizationId and n.user.id = :userId and n.id in :ids")
    Integer updateNotificationsStatus(Long organizationId, Long userId, List<Long> ids, NotificationStatus notificationStatus);

    @EntityGraph(attributePaths = {"trigger", "user"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<NotificationEntity> findByUserIdAndId(Long userId, Long id);
}
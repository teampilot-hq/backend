package app.teamwize.api.notification.repository;

import app.teamwize.api.notification.model.entity.NotificationEntity;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends BaseJpaRepository<NotificationEntity, Long>, JpaSpecificationExecutor<NotificationEntity> {
    List<NotificationEntity> findByOrganizationId(Long organizationId);

    @EntityGraph(attributePaths = {"trigger", "user"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<NotificationEntity> findAll(Specification<NotificationEntity> spec, Pageable pageable);
}
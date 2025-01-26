package app.teamwize.api.leave.repository;

import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.leave.model.entity.Leave;
import app.teamwize.api.leave.model.entity.LeavePolicyActivatedTypeId;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveRepository extends BaseJpaRepository<Leave, Long>, JpaSpecificationExecutor<Leave> {

    @EntityGraph(attributePaths = {"user", "user.team", "user.avatar", "type", "activatedType"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Leave> findByUserIdAndId(Long userId, Long id);


    @EntityGraph(attributePaths = {"user", "user.team", "user.avatar", "type", "activatedType"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<Leave> findByOrganizationIdAndUserId(Long organizationId, Long userId, Pageable page);


    @EntityGraph(attributePaths = {"user", "user.team", "user.avatar", "policy", "type", "activatedType"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<Leave> findAll(Specification<Leave> spec, Pageable pageable);

    @Query("select sum(l.duration) from Leave l where l.organization.id=:organizationId and l.user.id=:userId and l.activatedType.id=:activatedTypeId and l.status=:status")
    Float countByOrganizationIdAndUserIdAndTypeId(Long organizationId, Long userId, LeavePolicyActivatedTypeId activatedTypeId, LeaveStatus status);
}

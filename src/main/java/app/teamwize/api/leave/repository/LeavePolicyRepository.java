package app.teamwize.api.leave.repository;

import app.teamwize.api.leave.model.LeavePolicyStatus;
import app.teamwize.api.leave.model.entity.LeavePolicy;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeavePolicyRepository extends BaseJpaRepository<LeavePolicy, Long> {

    @EntityGraph(attributePaths = {"organization"}, type = EntityGraph.EntityGraphType.FETCH)
    List<LeavePolicy> findByOrganizationIdAndStatusIsIn(Long organizationId, List<LeavePolicyStatus> statuses);

    void deleteByOrganizationIdAndId(Long organizationId, Long id);

    @EntityGraph(attributePaths = {"organization"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<LeavePolicy> findByOrganizationIdAndId(Long organizationId, Long id);


    @Modifying
    @Query("update LeavePolicy policy set policy.status=:status where policy.organization.id=:organizationId and policy.id=:id")
    void updateStatus(Long organizationId, Long id,LeavePolicyStatus status);

}

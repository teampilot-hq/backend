package app.teamwize.api.leave.repository;

import app.teamwize.api.leave.model.entity.LeavePolicyApprover;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeavePolicyApproverRepository extends BaseJpaRepository<LeavePolicyApprover, Long> {
    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    List<LeavePolicyApprover> findByPolicyId(Long leavePolicyId);

    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    List<LeavePolicyApprover> findByPolicyIdIsIn(List<Long> leavePolicyIds);
}

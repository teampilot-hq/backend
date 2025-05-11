package app.teamwize.api.leave.repository;

import app.teamwize.api.leave.model.entity.LeavePolicyActivatedType;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LeavePolicyActivatedTypeRepository extends BaseJpaRepository<LeavePolicyActivatedType, Long> {
    @EntityGraph(attributePaths = {"type"}, type = EntityGraph.EntityGraphType.FETCH)
    List<LeavePolicyActivatedType> findByPolicyId(Long leavePolicyId);

    @EntityGraph(attributePaths = {"type"}, type = EntityGraph.EntityGraphType.FETCH)
    List<LeavePolicyActivatedType> findByPolicyIdIsIn(List<Long> leavePolicyIds);
}

package app.teamwize.api.leave.repository;

import app.teamwize.api.leave.model.entity.LeaveVote;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LeaveVoteRepository extends BaseJpaRepository<LeaveVote, Long> {

    List<LeaveVote> findByLeaveId(Long leaveId);
}

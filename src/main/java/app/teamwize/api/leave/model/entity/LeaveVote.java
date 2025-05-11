package app.teamwize.api.leave.model.entity;

import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.leave.model.LeaveVoteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "leave_votes")
public class LeaveVote extends BaseAuditEntity {
    @Id
    @GeneratedValue(generator = "leave_vote_id_seq_generator")
    @SequenceGenerator(name = "leave_vote_id_seq_generator", sequenceName = "leave_vote_id_seq", allocationSize = 1)
    protected Long id;

    private LeaveVoteStatus status;

    private Long userId;

    private Long leaveId;
}
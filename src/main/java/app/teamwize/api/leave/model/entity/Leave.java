package app.teamwize.api.leave.model.entity;

import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.organization.domain.entity.Organization;
import app.teamwize.api.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "leaves")
public class Leave extends BaseAuditEntity {
    @Id
    @GeneratedValue(generator = "day_off_id_seq_generator")
    @SequenceGenerator(name = "day_off_id_seq_generator", sequenceName = "day_off_id_seq", allocationSize = 1)
    protected Long id;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "policy_id", referencedColumnName = "policy_id"),
            @JoinColumn(name = "type_id", referencedColumnName = "type_id")
    })
    private LeavePolicyActivatedType activatedType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private LeaveType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", insertable = false, updatable = false)
    private LeavePolicy policy;


    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String reason;

    private Float duration;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organization organization;


    @PrePersist
    @PreUpdate
    private void syncReferences() {
        if (activatedType != null) {
            this.type = activatedType.getType();
            this.policy = activatedType.getPolicy();
        }
    }

}
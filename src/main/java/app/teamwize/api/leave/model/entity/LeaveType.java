package app.teamwize.api.leave.model.entity;

import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.base.domain.entity.EntityStatus;
import app.teamwize.api.leave.model.LeaveTypeCycle;
import app.teamwize.api.organization.domain.entity.Organization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "leave_types")
@NoArgsConstructor
public class LeaveType extends BaseAuditEntity {
    @Id
    @GeneratedValue(generator = "leave_type_id_seq_generator")
    @SequenceGenerator(name = "leave_type_id_seq_generator", sequenceName = "leave_type_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private LeaveTypeCycle cycle;
    @Enumerated(EnumType.STRING)
    private EntityStatus status;
    private String symbol;
    private Integer amount;
    private Boolean requiresApproval;

    @ManyToOne
    private Organization organization;


    public LeaveType(Long organizationId) {
        this.id = organizationId;
    }
}
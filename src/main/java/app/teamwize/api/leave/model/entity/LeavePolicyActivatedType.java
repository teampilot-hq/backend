package app.teamwize.api.leave.model.entity;

import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.base.domain.entity.EntityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "leave_policy_activated_types")
@NoArgsConstructor
public class LeavePolicyActivatedType extends BaseAuditEntity {

    @EmbeddedId
    private LeavePolicyActivatedTypeId id;

    private Integer amount;
    private Boolean requiresApproval;

    @ManyToOne
    @MapsId("typeId") // This maps to the typeId field in LeavePolicyActivatedTypeId
    @JoinColumn(name = "type_id")
    private LeaveType type;

    @ManyToOne
    @MapsId("policyId") // This maps to the policyId field in LeavePolicyActivatedTypeId
    @JoinColumn(name = "policy_id")
    private LeavePolicy policy;

    @Enumerated(EnumType.STRING)
    private EntityStatus status;
}

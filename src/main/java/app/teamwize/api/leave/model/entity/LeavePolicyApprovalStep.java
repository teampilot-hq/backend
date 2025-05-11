package app.teamwize.api.leave.model.entity;


import app.teamwize.api.leave.model.LeaveApproverCondition;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyApprovalStep {
    private Integer step;
    private LeaveApproverCondition condition;
}
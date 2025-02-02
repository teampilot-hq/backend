package app.teamwize.api.leave.repository;


import app.teamwize.api.leave.model.LeaveStatus;
import app.teamwize.api.leave.model.entity.Leave;
import app.teamwize.api.user.domain.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class LeaveSpecifications {

    public static Specification<Leave> hasOrganizationId(Long organizationId) {
        return (root, query, builder) -> builder.equal(root.get("organization").get("id"), organizationId);
    }

    public static Specification<Leave> hasTeamId(Long teamId) {
        return (root, query, builder) -> {
            Join<Leave, User> userJoin = root.join("user");
            return builder.equal(userJoin.get("team").get("id"), teamId);
        };
    }

    public static Specification<Leave> hasUserId(Long userId) {
        return (root, query, builder) -> builder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Leave> hasStatus(LeaveStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Leave> isEndedInBetween(Instant start, Instant end) {
        return (root, query, builder) -> builder.between(root.get("endAt"), start, end);
    }


    public static Specification<Leave> isStartedInBetween(Instant start, Instant end) {
        return (root, query, builder) -> builder.between(root.get("startAt"), start, end);
    }
}


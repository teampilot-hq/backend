package app.teamwize.api.user.domain.entity;


import app.teamwize.api.assets.domain.entity.Asset;
import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.leave.model.entity.LeavePolicy;
import app.teamwize.api.organization.domain.entity.Organization;
import app.teamwize.api.team.domain.entity.Team;
import app.teamwize.api.user.domain.UserRole;
import app.teamwize.api.user.domain.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends BaseAuditEntity {
    @Id
    @GeneratedValue(generator = "user_id_seq_generator")
    @SequenceGenerator(name = "user_id_seq_generator", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String email;
    private String password;
    private String phone;
    private String firstName;
    private String lastName;
    private String country;
    private String timezone;
    @ManyToOne(fetch = FetchType.LAZY)
    private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    private Asset avatar;

    @ManyToOne
    private LeavePolicy leavePolicy;

    public User(Long userId) {
        this.id = userId;
    }

    public ZoneId getTimeZoneId() {
        return ZoneId.of(Objects.requireNonNullElse(timezone, "UTC"));
    }

}

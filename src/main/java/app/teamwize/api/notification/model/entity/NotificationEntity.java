package app.teamwize.api.notification.model.entity;

import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationStatus;
import app.teamwize.api.organization.domain.entity.Organization;
import app.teamwize.api.user.domain.entity.User;
import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class NotificationEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(generator = "notification_id_seq_gen")
    @SequenceGenerator(name = "notification_id_seq_gen", sequenceName = "notification_id_seq", allocationSize = 10)
    private Long id;

    @ManyToOne
    private NotificationTriggerEntity trigger;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Long eventId;

    @ManyToOne
    private Organization organization;

    private String title;

    private String textContent;

    private String htmlContent;

    @ManyToOne
    private User user;

    @Type(JsonType.class)
    private Map<String, Object> params;

    @Type(StringArrayType.class)
    @Enumerated(EnumType.STRING)
    private NotificationChannel[] channels;

    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;


    @Type(JsonType.class)
    private Map<String, Object> metadata;
}

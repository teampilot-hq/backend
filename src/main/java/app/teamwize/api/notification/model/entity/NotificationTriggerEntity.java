package app.teamwize.api.notification.model.entity;

import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationTriggerReceptors;
import app.teamwize.api.notification.model.NotificationTriggerStatus;
import app.teamwize.api.organization.domain.entity.Organization;
import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Table(name = "notification_triggers")
public class NotificationTriggerEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(generator = "notification_trigger_id_seq_gen")
    @SequenceGenerator(name = "notification_trigger_id_seq_gen", sequenceName = "notification_trigger_id_seq", allocationSize = 10)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Type(StringArrayType.class)
    @Enumerated(EnumType.STRING)
    private NotificationChannel[] channels;

    @Enumerated(EnumType.STRING)
    private NotificationTriggerReceptors receptors;

    @Enumerated(EnumType.STRING)
    private NotificationTriggerStatus status;

    private String title;

    private String textTemplate;
    private String htmlTemplate;

    @ManyToOne
    private Organization organization;
}
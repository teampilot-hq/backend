package app.teamwize.api.event.entity;

import app.teamwize.api.base.domain.entity.BaseAuditEntity;
import app.teamwize.api.event.model.EventStatus;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.organization.domain.entity.Organization;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@Entity
@Table(name = "events")
public class EventEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(generator = "event_id_seq_gen")
    @SequenceGenerator(name = "event_id_seq_gen", sequenceName = "event_id_seq", allocationSize = 10)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Type(JsonType.class)
    private Map<String, Object> params;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private Byte maxAttempts;

    private Instant scheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventExecutionEntity> executions;
}

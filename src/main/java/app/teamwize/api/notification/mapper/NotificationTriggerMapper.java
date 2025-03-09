package app.teamwize.api.notification.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.notification.model.NotificationTrigger;
import app.teamwize.api.notification.model.command.NotificationTriggerCreateCommand;
import app.teamwize.api.notification.model.entity.NotificationTriggerEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = DefaultMapperConfig.class)
public interface NotificationTriggerMapper {
    NotificationTrigger toModel(NotificationTriggerEntity entity);

    NotificationTriggerEntity toEntity(NotificationTriggerCreateCommand notificationTrigger);

    NotificationTriggerEntity toEntity(NotificationTrigger notificationTrigger);

    List<NotificationTrigger> toModels(List<NotificationTriggerEntity> triggers);
}

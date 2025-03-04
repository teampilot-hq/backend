package app.teamwize.api.notification.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.notification.model.Notification;
import app.teamwize.api.notification.model.command.NotificationCreateCommand;
import app.teamwize.api.notification.model.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = DefaultMapperConfig.class)
public interface NotificationMapper {
    @Mapping(target = "trigger", ignore = true)
    NotificationEntity toEntity(NotificationCreateCommand command);

    Notification toModel(NotificationEntity entity);

    List<Notification> toModels(List<NotificationEntity> entities);
}

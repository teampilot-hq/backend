package app.teamwize.api.notification.rest.mapper;

import app.teamwize.api.base.config.DefaultMapperConfig;
import app.teamwize.api.notification.model.Notification;
import app.teamwize.api.notification.model.NotificationTrigger;
import app.teamwize.api.notification.model.command.NotificationFilterCommand;
import app.teamwize.api.notification.model.command.NotificationTriggerCreateCommand;
import app.teamwize.api.notification.model.command.NotificationTriggerUpdateCommand;
import app.teamwize.api.notification.rest.model.request.NotificationFilterRequest;
import app.teamwize.api.notification.rest.model.request.NotificationTriggerCreateRequest;
import app.teamwize.api.notification.rest.model.request.NotificationTriggerUpdateRequest;
import app.teamwize.api.notification.rest.model.response.NotificationResponse;
import app.teamwize.api.notification.rest.model.response.NotificationTriggerResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = DefaultMapperConfig.class)
public interface NotificationRestMapper {
    NotificationFilterCommand toCommand(NotificationFilterRequest command);

    List<NotificationResponse> toResponses(List<Notification> contents);


    NotificationTriggerCreateCommand toCommand(NotificationTriggerCreateRequest request);

    NotificationTriggerResponse toResponse(NotificationTrigger result);

    NotificationTriggerUpdateCommand toCommand(NotificationTriggerUpdateRequest request);

    NotificationResponse toResponse(Notification notification);
}

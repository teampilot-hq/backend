package app.teamwize.api.notification.service;

import app.teamwize.api.base.domain.model.Paged;
import app.teamwize.api.base.domain.model.request.PaginationRequest;
import app.teamwize.api.event.service.EventService;
import app.teamwize.api.notification.config.NotificationConfigModel;
import app.teamwize.api.notification.exception.NotificationNotFoundException;
import app.teamwize.api.notification.exception.NotificationTemplateCompileException;
import app.teamwize.api.notification.exception.NotificationTriggerNotFoundException;
import app.teamwize.api.notification.mapper.NotificationMapper;
import app.teamwize.api.notification.mapper.NotificationTriggerMapper;
import app.teamwize.api.notification.model.Notification;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationStatus;
import app.teamwize.api.notification.model.command.NotificationCreateCommand;
import app.teamwize.api.notification.model.command.NotificationFilterCommand;
import app.teamwize.api.notification.model.event.NotificationCreatedEvent;
import app.teamwize.api.notification.model.event.NotificationEventPayload;
import app.teamwize.api.notification.repository.NotificationRepository;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
import app.teamwize.api.user.service.UserService;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.StringTemplateSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static app.teamwize.api.notification.repository.NotificationSpecifications.*;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationTriggerService triggerService;
    private final NotificationTriggerMapper notificationTriggerMapper;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final NotificationConfigModel config;
    @Lazy
    private final EventService eventService;
    private final Handlebars handlebars;

    @Transactional
    public Notification createNotification(Long organizationId, NotificationCreateCommand command)
            throws NotificationTriggerNotFoundException, OrganizationNotFoundException, NotificationTemplateCompileException {
        var entity = notificationMapper.toEntity(command);

        // Set template and trigger from their IDs

        var trigger = triggerService.getNotificationTrigger(organizationId, command.triggerId());
        var organization = organizationService.getOrganization(organizationId);

        entity.getParams().put("baseUrl", config.email().baseUrl());

        try {
            var textContent = handlebars.compile(new StringTemplateSource("notification", trigger.textTemplate())).apply(entity.getParams());
            var htmlContent = handlebars.compile(new StringTemplateSource("notification", trigger.htmlTemplate())).apply(entity.getParams());
            var title = handlebars.compile(new StringTemplateSource("title", trigger.title())).apply(entity.getParams());
            entity
                    .setTitle(title)
                    .setHtmlContent(htmlContent)
                    .setTextContent(textContent);
        } catch (Exception e) {
            throw new NotificationTemplateCompileException("Error compiling template : " + e.getMessage());
        }
        entity
                .setSentAt(Instant.now())
                .setChannels(trigger.channels() == null ? null : trigger.channels().toArray(new NotificationChannel[0]))
                .setEventType(command.event())
                .setEventId(command.eventId())
                .setStatus(NotificationStatus.PENDING)
                .setTrigger(notificationTriggerMapper.toEntity(trigger))
                .setUser(command.user())
                .setOrganization(organization);

        entity = notificationRepository.persist(entity);
        var model = notificationMapper.toModel(entity);
        eventService.emmit(organizationId, new NotificationCreatedEvent(new NotificationEventPayload(model)));
        return model;
    }

    @Transactional(readOnly = true)
    public Notification getNotification(Long userId, Long id) throws NotificationNotFoundException {
        var entity = notificationRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        return notificationMapper.toModel(entity);
    }



    @Transactional(readOnly = true)
    public Paged<Notification> getNotifications(
            Long organizationId,
            NotificationFilterCommand command,
            PaginationRequest pagination) {
        var sort = Sort.by("id").descending();
        var pageRequest = PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), sort);

        var specs = Specification.where(withOrganizationId(organizationId))
                .and(withStatus(command.status()))
                .and(withEventType(command.eventType()));

        var page = notificationRepository.findAll(specs, pageRequest)
                .map(notificationMapper::toModel);

        return new Paged<>(
                page.getContent(),
                pagination.getPageNumber(),
                pagination.getPageSize(),
                page.getTotalElements()
        );
    }

    public Long getNotificationsCount(Long organizationId, Long userId, List<NotificationStatus> statuses) {
        return notificationRepository.countByOrganizationIdAndUserIdAndStatusIsIn(organizationId, userId, statuses);
    }

    @Transactional
    public Integer markAsRead(Long organizationId, Long userId, List<Long> ids) {
        return notificationRepository.updateNotificationsStatus(organizationId, userId, ids, NotificationStatus.READ);
    }

    @Transactional
    public Integer updateNotificationStatus(Long organizationId, Long userId, Long id, NotificationStatus status) {
        return notificationRepository.updateNotificationsStatus(organizationId, userId, List.of(id), status);
    }
}

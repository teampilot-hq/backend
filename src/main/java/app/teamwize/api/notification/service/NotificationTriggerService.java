package app.teamwize.api.notification.service;

import app.teamwize.api.event.model.EventType;
import app.teamwize.api.notification.exception.NotificationTriggerNotFoundException;
import app.teamwize.api.notification.mapper.NotificationTriggerMapper;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationTrigger;
import app.teamwize.api.notification.model.NotificationTriggerStatus;
import app.teamwize.api.notification.model.command.NotificationTriggerCreateCommand;
import app.teamwize.api.notification.model.command.NotificationTriggerUpdateCommand;
import app.teamwize.api.notification.repository.NotificationTriggerRepository;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationTriggerService {

    private final NotificationTriggerRepository notificationTriggerRepository;
    private final NotificationTriggerMapper notificationTriggerMapper;
    private final OrganizationService organizationService;

    @Transactional
    public NotificationTrigger createNotificationTrigger(Long organizationId, NotificationTriggerCreateCommand notificationTrigger) throws OrganizationNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        var entity = notificationTriggerMapper.toEntity(notificationTrigger)
                .setTitle(notificationTrigger.title())
                .setStatus(NotificationTriggerStatus.ENABLED)
                .setOrganization(organization);
        entity = notificationTriggerRepository.persist(entity);
        return notificationTriggerMapper.toModel(entity);
    }

    public List<NotificationTrigger> getNotificationTriggers(Long organizationId, EventType eventType) {
        var triggers = notificationTriggerRepository.findByOrganizationIdAndEventTypeAndStatus(
                organizationId,
                eventType,
                NotificationTriggerStatus.ENABLED
        );
        return notificationTriggerMapper.toModels(triggers);
    }

    public List<NotificationTrigger> getNotificationTriggers(Long organizationId) {
        var triggers = notificationTriggerRepository.findByOrganizationId(
                organizationId
        );
        return notificationTriggerMapper.toModels(triggers);
    }

    public NotificationTrigger getNotificationTrigger(Long organizationId, Long id) throws NotificationTriggerNotFoundException {
        return notificationTriggerRepository.findByOrganizationIdAndId(organizationId, id)
                .map(notificationTriggerMapper::toModel)
                .orElseThrow(() -> new NotificationTriggerNotFoundException("Notification trigger not found"));
    }

    public void deleteTrigger(Long organizationId, Long id) throws NotificationTriggerNotFoundException {
        var trigger = notificationTriggerRepository.findByOrganizationIdAndId(organizationId, id)
                .orElseThrow(() -> new NotificationTriggerNotFoundException("Notification trigger not found"));

        trigger.setStatus(NotificationTriggerStatus.ARCHIVED);
        notificationTriggerRepository.merge(trigger);
    }

    public NotificationTrigger updateTrigger(Long organizationId, Long id, NotificationTriggerUpdateCommand request) throws NotificationTriggerNotFoundException {
        var trigger = notificationTriggerRepository.findByOrganizationIdAndId(organizationId, id)
                .orElseThrow(() -> new NotificationTriggerNotFoundException("Notification trigger not found"));


        trigger.setChannels(request.channels() == null ? null : request.channels().toArray(new NotificationChannel[0]))
                .setName(request.name())
                .setTitle(request.title())
                .setEventType(request.eventType())
                .setHtmlTemplate(request.htmlTemplate())
                .setTextTemplate(request.textTemplate())
                .setReceptors(request.receptors());

        trigger = notificationTriggerRepository.merge(trigger);
        return notificationTriggerMapper.toModel(trigger);
    }
}

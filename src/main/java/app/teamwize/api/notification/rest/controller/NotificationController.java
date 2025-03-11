package app.teamwize.api.notification.rest.controller;

import app.teamwize.api.auth.service.SecurityService;
import app.teamwize.api.base.domain.model.request.PaginationRequest;
import app.teamwize.api.base.domain.model.response.PagedResponse;
import app.teamwize.api.base.mapper.PagedResponseMapper;
import app.teamwize.api.notification.exception.NotificationNotFoundException;
import app.teamwize.api.notification.exception.NotificationTriggerNotFoundException;
import app.teamwize.api.notification.model.NotificationStatus;
import app.teamwize.api.notification.rest.mapper.NotificationRestMapper;
import app.teamwize.api.notification.rest.model.request.NotificationFilterRequest;
import app.teamwize.api.notification.rest.model.request.NotificationTriggerCreateRequest;
import app.teamwize.api.notification.rest.model.request.NotificationTriggerUpdateRequest;
import app.teamwize.api.notification.rest.model.response.NotificationResponse;
import app.teamwize.api.notification.rest.model.response.NotificationTriggerResponse;
import app.teamwize.api.notification.rest.model.response.NotificationsCountResponse;
import app.teamwize.api.notification.service.EventSchemaService;
import app.teamwize.api.notification.service.NotificationService;
import app.teamwize.api.notification.service.NotificationTriggerService;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationTriggerService notificationTriggerService;
    private final SecurityService securityService;
    private final NotificationRestMapper notificationRestMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final EventSchemaService eventSchemaService;


    @GetMapping
    public PagedResponse<NotificationResponse> getNotifications(@ParameterObject @Valid NotificationFilterRequest request,
                                                                @ParameterObject @Valid PaginationRequest pagination) {
        var result = notificationService.getNotifications(securityService.getUserId(),
                notificationRestMapper.toCommand(request),
                pagination
        );
        return pagedResponseMapper.toPagedResponse(
                notificationRestMapper.toResponses(result.contents()),
                result.pageNumber(),
                result.pageSize(),
                result.totalPages(),
                result.totalContents()
        );
    }

    @GetMapping("{id}")
    public NotificationResponse getNotification(@PathVariable Long id) throws NotificationNotFoundException {
        var notification = notificationService.getNotification(securityService.getUserOrganizationId(), id);
        return notificationRestMapper.toResponse(notification);
    }


    @GetMapping("count")
    public NotificationsCountResponse getUnreadNotificationsCount() {
        var unreadCount = notificationService.getNotificationsCount(securityService.getUserOrganizationId(), securityService.getUserId(), List.of(NotificationStatus.SENT));
        var totalCount = notificationService.getNotificationsCount(securityService.getUserOrganizationId(), securityService.getUserId(), List.of(NotificationStatus.SENT, NotificationStatus.READ));
        return new NotificationsCountResponse(unreadCount, totalCount);
    }

    @PostMapping("read")
    public NotificationsCountResponse markAsRead(@RequestBody List<Long> ids) {
        notificationService.markAsRead(securityService.getUserOrganizationId(), securityService.getUserId(), ids);
        return getUnreadNotificationsCount();
    }

    @PostMapping("triggers")
    public NotificationTriggerResponse createNotificationTrigger(@RequestBody NotificationTriggerCreateRequest request) throws OrganizationNotFoundException {
        var result = notificationTriggerService.createNotificationTrigger(securityService.getUserOrganizationId(), notificationRestMapper.toCommand(request));
        return notificationRestMapper.toResponse(result);
    }

    @GetMapping("triggers")
    public List<NotificationTriggerResponse> getNotificationTriggers() {
        return notificationTriggerService.getNotificationTriggers(securityService.getUserOrganizationId()).stream().map(
                notificationRestMapper::toResponse
        ).toList();
    }


    @PutMapping("/triggers/{id}")
    public NotificationTriggerResponse updateTemplate(@PathVariable Long id,
                                                      @RequestBody NotificationTriggerUpdateRequest request) throws NotificationTriggerNotFoundException {
        var result = notificationTriggerService.updateTrigger(securityService.getUserOrganizationId(), id, notificationRestMapper.toCommand(request));
        return notificationRestMapper.toResponse(result);
    }

    @GetMapping("/triggers/{id}")
    public NotificationTriggerResponse getTrigger(@PathVariable Long id) throws NotificationTriggerNotFoundException {
        var trigger = notificationTriggerService.getNotificationTrigger(securityService.getUserOrganizationId(), id);
        return notificationRestMapper.toResponse(trigger);
    }


    @DeleteMapping("/triggers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrigger(@PathVariable Long id) throws NotificationTriggerNotFoundException {
        notificationTriggerService.deleteTrigger(securityService.getUserOrganizationId(), id);
    }

    @GetMapping("events")
    public List<EventSchemaService.EventSchema> getEvents() {
        return eventSchemaService.getEventSchemas();
    }


}

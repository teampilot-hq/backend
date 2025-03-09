package app.teamwize.api.notification.listener;

import app.teamwize.api.event.model.Event;
import app.teamwize.api.event.model.EventExitCode;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.event.service.handler.EventHandler;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.NotificationTriggerReceptors;
import app.teamwize.api.notification.model.command.NotificationTriggerCreateCommand;
import app.teamwize.api.notification.service.NotificationTriggerService;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateDefaultNotificationTriggersEventHandler implements EventHandler {

    static final String DEFAULT_LEAVE_CREATE_USER_NOTIFICATION_TITLE = "Your Leave Request #{{leave.id}} Has Been Created";
    static final String DEFAULT_LEAVE_CREATE_USER_NOTIFICATION_NAME = "Notify user when leave is created";
    static final String DEFAULT_LEAVE_CREATE_REVIEWER_NOTIFICATION_TITLE = "New Leave Request - {{user.firstName}} {{user.lastName}}";
    static final String DEFAULT_LEAVE_CREATE_REVIEWER_NOTIFICATION_NAME = "Notify reviewers when leave is created";
    static final String DEFAULT_LEAVE_UPDATE_USER_NOTIFICATION_TITLE = "Update on Your Leave Request #{{leave.id}}";
    static final String DEFAULT_LEAVE_UPDATE_USER_NOTIFICATION_NAME = "Notify user when leave is updated";
    private final NotificationTriggerService notificationTriggerService;
    @Value("classpath:/templates/email/default-leave-created-reviewer.html")
    private Resource defaultLeaveCreatedReviewerTemplateHTML;
    @Value("classpath:/templates/email/default-leave-created-reviewer.txt")
    private Resource defaultLeaveCreatedReviewerTemplateTXT;
    @Value("classpath:/templates/email/default-leave-updated-user.html")
    private Resource defaultLeaveUpdatedUserTemplateHTML;
    @Value("classpath:/templates/email/default-leave-updated-user.txt")
    private Resource defaultLeaveUpdatedUserTemplateTXT;
    @Value("classpath:/templates/email/default-leave-created-user.html")
    private Resource defaultLeaveCreatedUserTemplateHTML;
    @Value("classpath:/templates/email/default-leave-created-user.txt")
    private Resource defaultLeaveCreatedUserTemplateTXT;

    @Override
    public String name() {
        return "DefaultNotificationTriggers";
    }

    @Override
    public boolean accepts(EventType type) {
        return type == EventType.ORGANIZATION_CREATED;
    }

    @Override
    public EventExecutionResult process(Event event) {
        try {
            // sending notification to user when leave is created
            notificationTriggerService.createNotificationTrigger(event.organization().getId(), new NotificationTriggerCreateCommand(
                    DEFAULT_LEAVE_CREATE_USER_NOTIFICATION_NAME,
                    DEFAULT_LEAVE_CREATE_USER_NOTIFICATION_TITLE,
                    defaultLeaveCreatedUserTemplateTXT.getContentAsString(Charset.defaultCharset()),
                    defaultLeaveCreatedUserTemplateHTML.getContentAsString(Charset.defaultCharset()),
                    EventType.LEAVE_CREATED,
                    List.of(NotificationChannel.EMAIL),
                    NotificationTriggerReceptors.USER
            ));

            // sending notification to user when leave is created
            notificationTriggerService.createNotificationTrigger(event.organization().getId(), new NotificationTriggerCreateCommand(
                    DEFAULT_LEAVE_CREATE_REVIEWER_NOTIFICATION_NAME,
                    DEFAULT_LEAVE_CREATE_REVIEWER_NOTIFICATION_TITLE,
                    defaultLeaveCreatedReviewerTemplateTXT.getContentAsString(Charset.defaultCharset()),
                    defaultLeaveCreatedReviewerTemplateHTML.getContentAsString(Charset.defaultCharset()),
                    EventType.LEAVE_CREATED,
                    List.of(NotificationChannel.EMAIL),
                    NotificationTriggerReceptors.REVIEWERS
            ));

            // sending notification to user when leave is updated
            notificationTriggerService.createNotificationTrigger(event.organization().getId(), new NotificationTriggerCreateCommand(
                    DEFAULT_LEAVE_UPDATE_USER_NOTIFICATION_NAME,
                    DEFAULT_LEAVE_UPDATE_USER_NOTIFICATION_TITLE,
                    defaultLeaveUpdatedUserTemplateTXT.getContentAsString(Charset.defaultCharset()),
                    defaultLeaveUpdatedUserTemplateHTML.getContentAsString(Charset.defaultCharset()),
                    EventType.LEAVE_STATUS_UPDATED,
                    List.of(NotificationChannel.EMAIL),
                    NotificationTriggerReceptors.USER
            ));

            return new EventExecutionResult(EventExitCode.SUCCESS, null);
        } catch (OrganizationNotFoundException | IOException e) {
            return new EventExecutionResult(EventExitCode.ERROR, null);
        }
    }
}
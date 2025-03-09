package app.teamwize.api.notification.service.notifier;

import app.teamwize.api.notification.exception.NotificationSendFailureException;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.event.NotificationEventPayload;


public interface Notifier {
    boolean accepts(NotificationChannel channel);

    void notify(NotificationEventPayload event) throws NotificationSendFailureException;
}

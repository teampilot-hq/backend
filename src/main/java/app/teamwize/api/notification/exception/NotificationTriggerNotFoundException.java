package app.teamwize.api.notification.exception;

import app.teamwize.api.base.exception.NotFoundException;

public class NotificationTriggerNotFoundException extends NotFoundException {


    public NotificationTriggerNotFoundException(String message) {
        super(message);
    }
}

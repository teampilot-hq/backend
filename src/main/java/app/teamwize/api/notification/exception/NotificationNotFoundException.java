package app.teamwize.api.notification.exception;

import app.teamwize.api.base.exception.NotFoundException;

public class NotificationNotFoundException extends NotFoundException {


    public NotificationNotFoundException(String message) {
        super(message);
    }
}

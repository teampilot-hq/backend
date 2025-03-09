package app.teamwize.api.notification.exception;

import app.teamwize.api.base.exception.ServerException;

public class NotificationSendFailureException extends ServerException {
    public NotificationSendFailureException(String message) {
        super(message);
    }
}

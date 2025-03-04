package app.teamwize.api.notification.exception;

import app.teamwize.api.base.exception.ServerException;

public class NotificationTemplateCompileException extends ServerException {
    public NotificationTemplateCompileException(String message) {
        super(message);
    }
}

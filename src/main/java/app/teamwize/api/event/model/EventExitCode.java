package app.teamwize.api.event.model;

public enum EventExitCode {
    SUCCESS,          // Event processed successfully without any issues

    RETRY_EXCEEDED,   // Maximum number of retry attempts exceeded; event failed despite multiple processing attempts

    HANDLER_NOT_FOUND,// No appropriate handler was found for the event type; event could not be processed due to lack of support

    ERROR, // An error occurred during event processing that prevented successful completion

    CONNECTION_ERROR, // Failed to connect to an external service required for event processing (e.g., email/SMS server)

    TIMEOUT,          // Event processing exceeded the allowable time limit and was stopped to avoid hanging

    CANCELED          // Event was canceled deliberately and was not processed
}
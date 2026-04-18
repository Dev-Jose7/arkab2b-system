package com.arka.notification.domain.notificationdispatch.exception;

public class NotificationTerminalStateException extends NotificationDomainException {

    public NotificationTerminalStateException(String status) {
        super(
                "notificacion_terminal",
                "La solicitud de notificacion esta en estado terminal: " + status);
    }
}

package com.arka.notification.domain.notificationdispatch.exception;

public class DiscardedNotificationCannotDispatchException extends NotificationDomainException {

    public DiscardedNotificationCannotDispatchException() {
        super(
                "notificacion_descartada_sin_retry",
                "Una solicitud descartada no admite dispatch ni retry");
    }
}

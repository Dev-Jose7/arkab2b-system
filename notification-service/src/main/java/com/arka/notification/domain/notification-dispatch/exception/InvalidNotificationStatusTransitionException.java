package com.arka.notification.domain.notificationdispatch.exception;

public class InvalidNotificationStatusTransitionException extends NotificationDomainException {

    public InvalidNotificationStatusTransitionException(String from, String to) {
        super(
                "transicion_estado_notificacion_invalida",
                "No se permite transicion de estado " + from + " -> " + to);
    }
}

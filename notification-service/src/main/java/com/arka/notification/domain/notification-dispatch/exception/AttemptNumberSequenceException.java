package com.arka.notification.domain.notificationdispatch.exception;

public class AttemptNumberSequenceException extends NotificationDomainException {

    public AttemptNumberSequenceException(int expected, int received) {
        super(
                "secuencia_intento_invalida",
                "attempt_number esperado=" + expected + " recibido=" + received);
    }
}

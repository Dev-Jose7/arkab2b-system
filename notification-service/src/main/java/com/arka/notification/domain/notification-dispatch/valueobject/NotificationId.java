package com.arka.notification.domain.notificationdispatch.valueobject;

import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record NotificationId(String value) {

    public NotificationId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("notificacion_invalida", "notificationId es obligatorio");
        }
        value = value.trim();
    }

    public static NotificationId of(String value) {
        return new NotificationId(value);
    }

    public static NotificationId newId() {
        return new NotificationId(UUID.randomUUID().toString());
    }
}

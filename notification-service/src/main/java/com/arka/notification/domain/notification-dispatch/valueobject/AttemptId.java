package com.arka.notification.domain.notificationdispatch.valueobject;

import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record AttemptId(String value) {

    public AttemptId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("intento_invalido", "attemptId es obligatorio");
        }
        value = value.trim();
    }

    public static AttemptId of(String value) {
        return new AttemptId(value);
    }

    public static AttemptId newId() {
        return new AttemptId(UUID.randomUUID().toString());
    }
}

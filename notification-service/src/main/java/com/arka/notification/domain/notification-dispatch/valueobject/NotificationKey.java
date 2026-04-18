package com.arka.notification.domain.notificationdispatch.valueobject;

import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;

public record NotificationKey(String value) {

    public NotificationKey {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("notification_key_invalida", "notificationKey es obligatoria");
        }
        value = value.trim();
    }

    public static NotificationKey of(String value) {
        return new NotificationKey(value);
    }

    public static NotificationKey fromEventRecipientAndChannel(
            String sourceEventId,
            String recipientRef,
            NotificationChannel channel) {
        if (sourceEventId == null || sourceEventId.isBlank()) {
            throw new DomainInvariantViolationException("notification_key_invalida", "sourceEventId es obligatorio");
        }
        if (recipientRef == null || recipientRef.isBlank()) {
            throw new DomainInvariantViolationException("notification_key_invalida", "recipientRef es obligatorio");
        }
        return new NotificationKey(
                sourceEventId.trim() + "::" + recipientRef.trim() + "::" + channel.name());
    }
}

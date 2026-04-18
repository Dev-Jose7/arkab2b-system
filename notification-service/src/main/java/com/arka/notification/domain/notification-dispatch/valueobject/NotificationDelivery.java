package com.arka.notification.domain.notificationdispatch.valueobject;

import java.time.Instant;

public record NotificationDelivery(
        NotificationId notificationId,
        AttemptId attemptId,
        String providerCode,
        String providerRef,
        boolean delivered,
        Instant recordedAt) {
}

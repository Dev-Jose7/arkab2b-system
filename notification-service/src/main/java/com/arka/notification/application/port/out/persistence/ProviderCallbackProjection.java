package com.arka.notification.application.port.out.persistence;

import java.time.Instant;

public record ProviderCallbackProjection(
        String callbackId,
        String notificationId,
        String providerCode,
        String providerRef,
        String callbackEventId,
        String callbackStatus,
        String payload,
        Instant receivedAt,
        Instant updatedAt) {
}

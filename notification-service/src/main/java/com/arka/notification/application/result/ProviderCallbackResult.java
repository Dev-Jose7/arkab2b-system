package com.arka.notification.application.result;

import java.time.Instant;

public record ProviderCallbackResult(
        String callbackId,
        String notificationId,
        String providerCode,
        String providerRef,
        String callbackEventId,
        String callbackStatus,
        Instant receivedAt,
        Instant updatedAt) {
}

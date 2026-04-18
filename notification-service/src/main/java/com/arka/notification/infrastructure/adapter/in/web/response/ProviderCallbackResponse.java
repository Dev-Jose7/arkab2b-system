package com.arka.notification.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record ProviderCallbackResponse(
        String callbackId,
        String notificationId,
        String providerCode,
        String providerRef,
        String callbackEventId,
        String callbackStatus,
        Instant receivedAt,
        Instant updatedAt) {
}

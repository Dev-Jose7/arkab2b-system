package com.arka.notification.domain.notificationdispatch.entity;

import com.arka.notification.domain.notificationdispatch.enumtype.ProviderCallbackStatus;
import java.time.Instant;

public record ProviderCallback(
        String callbackId,
        String organizationId,
        String notificationId,
        String providerCode,
        String providerRef,
        String callbackEventId,
        ProviderCallbackStatus callbackStatus,
        String payload,
        Instant receivedAt,
        Instant updatedAt) {
}

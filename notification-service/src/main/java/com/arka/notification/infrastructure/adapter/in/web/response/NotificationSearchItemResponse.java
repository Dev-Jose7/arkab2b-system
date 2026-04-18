package com.arka.notification.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record NotificationSearchItemResponse(
        String notificationId,
        String sourceEventType,
        String recipientRef,
        String channel,
        String status,
        int attemptCount,
        Instant updatedAt) {
}

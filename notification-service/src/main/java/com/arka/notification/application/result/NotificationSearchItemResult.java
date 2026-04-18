package com.arka.notification.application.result;

import java.time.Instant;

public record NotificationSearchItemResult(
        String notificationId,
        String sourceEventType,
        String recipientRef,
        String channel,
        String status,
        int attemptCount,
        Instant updatedAt) {
}

package com.arka.notification.application.port.out.persistence;

import java.time.Instant;

public record NotificationSearchProjection(
        String notificationId,
        String sourceEventType,
        String recipientRef,
        String channel,
        String status,
        Integer attemptCount,
        Instant updatedAt) {
}

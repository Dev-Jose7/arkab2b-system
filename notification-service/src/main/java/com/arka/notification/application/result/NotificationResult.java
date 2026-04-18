package com.arka.notification.application.result;

import java.time.Instant;

public record NotificationResult(
        String notificationId,
        String organizationId,
        String sourceEventId,
        String sourceEventType,
        String recipientRef,
        String channel,
        String templateId,
        String channelPolicyId,
        String status,
        boolean retryable,
        Instant nextRetryAt,
        int maxAttempts,
        int attemptCount,
        String notificationKey,
        String traceId,
        String correlationId,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}

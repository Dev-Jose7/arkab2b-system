package com.arka.notification.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record NotificationResponse(
        String notificationId,
        String tenantId,
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

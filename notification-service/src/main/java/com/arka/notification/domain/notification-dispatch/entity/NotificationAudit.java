package com.arka.notification.domain.notificationdispatch.entity;

import java.time.Instant;

public record NotificationAudit(
        String auditId,
        String organizationId,
        String actorId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        String idempotencyKey,
        String payloadHash,
        Instant createdAt) {
}

package com.arka.notification.application.port.out.audit;

import java.time.Instant;

public record NotificationAuditEntry(
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

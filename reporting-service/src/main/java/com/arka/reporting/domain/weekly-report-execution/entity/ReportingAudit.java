package com.arka.reporting.domain.weeklyreportexecution.entity;

import java.time.Instant;

public record ReportingAudit(
        String auditId,
        String tenantId,
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

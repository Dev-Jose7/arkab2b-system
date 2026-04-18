package com.arka.reporting.application.port.out.audit;

import java.time.Instant;

public record ReportingAuditEntry(
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

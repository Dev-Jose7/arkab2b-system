package com.arka.reporting.application.result;

import java.time.Instant;

public record ReportingAuditEntryResult(
        String auditId,
        String organizationId,
        String actorId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        String idempotencyKey,
        Instant createdAt) {
}

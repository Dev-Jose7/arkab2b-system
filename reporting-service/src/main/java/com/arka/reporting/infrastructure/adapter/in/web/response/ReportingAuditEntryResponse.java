package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record ReportingAuditEntryResponse(
        String auditId,
        String organizationId,
        String actorId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        String idempotencyKey,
        Instant createdAt) {}

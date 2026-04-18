package com.arka.order.application.result;

import java.time.Instant;

public record OrderAuditEntryResult(
        String auditId,
        String organizationId,

        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {}

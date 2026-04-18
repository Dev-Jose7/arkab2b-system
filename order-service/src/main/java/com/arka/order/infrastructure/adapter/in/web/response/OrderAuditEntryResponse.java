package com.arka.order.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record OrderAuditEntryResponse(
        String auditId,
        String tenantId,
        String organizationId,
        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {}

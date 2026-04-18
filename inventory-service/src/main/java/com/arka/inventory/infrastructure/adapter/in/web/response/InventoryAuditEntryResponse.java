package com.arka.inventory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record InventoryAuditEntryResponse(
        String auditId,
        String organizationId,
        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {}

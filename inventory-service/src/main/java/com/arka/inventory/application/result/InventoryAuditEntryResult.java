package com.arka.inventory.application.result;

import java.time.Instant;

public record InventoryAuditEntryResult(
        String auditId,
        String organizationId,
        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {}

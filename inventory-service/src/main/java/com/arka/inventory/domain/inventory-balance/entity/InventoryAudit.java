package com.arka.inventory.domain.inventorybalance.entity;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record InventoryAudit(
        String auditId,
        String tenantId,
        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {

    public InventoryAudit {
        requireNotBlank(auditId, "auditId");
        requireNotBlank(tenantId, "tenantId");
        requireNotBlank(actorUserId, "actorUserId");
        requireNotBlank(actionType, "actionType");
        requireNotBlank(targetType, "targetType");
        requireNotBlank(targetId, "targetId");
        requireNotBlank(outcome, "outcome");
        payload = payload == null ? "{}" : payload;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}

package com.arka.inventory.domain.inventorybalance.entity;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record IdempotencyRecord(
        String idempotencyId,
        String tenantId,
        String operationName,
        String idempotencyKey,
        String requestHash,
        String resourceType,
        String resourceId,
        int responseStatus,
        Instant createdAt,
        Instant updatedAt) {

    public IdempotencyRecord {
        requireNotBlank(idempotencyId, "idempotencyId");
        requireNotBlank(tenantId, "tenantId");
        requireNotBlank(operationName, "operationName");
        requireNotBlank(idempotencyKey, "idempotencyKey");
        requireNotBlank(requestHash, "requestHash");
        responseStatus = responseStatus <= 0 ? 200 : responseStatus;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}

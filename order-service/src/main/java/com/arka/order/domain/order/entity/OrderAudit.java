package com.arka.order.domain.order.entity;

import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record OrderAudit(
        String auditId,
        String organizationId,

        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {

    public OrderAudit {
        requireNotBlank(auditId, "auditId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(organizationId, "organizationId");
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

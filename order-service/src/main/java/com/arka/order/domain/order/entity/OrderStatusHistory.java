package com.arka.order.domain.order.entity;

import com.arka.order.domain.order.enumtype.OrderStatus;
import com.arka.order.domain.order.exception.OrderConsistencyException;
import java.time.Instant;

public record OrderStatusHistory(
        String statusHistoryId,
        String orderId,
        String tenantId,
        String actorUserId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String reason,
        Instant occurredAt) {

    public OrderStatusHistory {
        requireNotBlank(statusHistoryId, "statusHistoryId");
        requireNotBlank(orderId, "orderId");
        requireNotBlank(tenantId, "tenantId");
        requireNotBlank(actorUserId, "actorUserId");
        if (toStatus == null) {
            throw new OrderConsistencyException("toStatus is required");
        }
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new OrderConsistencyException(fieldName + " is required");
        }
    }
}

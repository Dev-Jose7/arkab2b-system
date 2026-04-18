package com.arka.order.domain.order.entity;

import com.arka.order.domain.order.exception.OrderConsistencyException;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderLine(
        String orderLineId,
        String orderId,
        String organizationId,

        String variantId,
        String sku,
        int qty,
        BigDecimal unitPrice,
        String currency,
        String reservationId,
        boolean reservationConfirmed,
        BigDecimal lineTotal,
        Instant createdAt,
        Instant updatedAt) {

    public OrderLine {
        requireNotBlank(orderLineId, "orderLineId");
        requireNotBlank(orderId, "orderId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(variantId, "variantId");
        requireNotBlank(sku, "sku");
        requireNotBlank(currency, "currency");
        requireNotBlank(reservationId, "reservationId");
        if (qty <= 0) {
            throw new OrderConsistencyException("order line qty must be positive");
        }
        if (!reservationConfirmed) {
            throw new OrderConsistencyException("order line reservation must be confirmed");
        }
        unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        if (unitPrice.signum() < 0) {
            throw new OrderConsistencyException("order line unitPrice cannot be negative");
        }
        lineTotal = lineTotal == null
                ? unitPrice.multiply(BigDecimal.valueOf(qty))
                : lineTotal;
        if (lineTotal.signum() < 0) {
            throw new OrderConsistencyException("order line total cannot be negative");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new OrderConsistencyException(fieldName + " is required");
        }
    }
}

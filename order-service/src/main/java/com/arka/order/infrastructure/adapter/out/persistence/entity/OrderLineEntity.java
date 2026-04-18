package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_lines")
public record OrderLineEntity(
        @Id String orderLineId,
        String orderId,
        String tenantId,
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
        Instant updatedAt) {}

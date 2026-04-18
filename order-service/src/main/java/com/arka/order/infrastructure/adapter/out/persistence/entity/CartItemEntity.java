package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_items")
public record CartItemEntity(
        @Id String cartItemId,
        String cartId,
        String tenantId,
        String organizationId,
        String variantId,
        String sku,
        int qty,
        BigDecimal unitPrice,
        String currency,
        String reservationId,
        boolean reservationConfirmed,
        Instant createdAt,
        Instant updatedAt) {}

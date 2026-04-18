package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("purchase_orders")
public record PurchaseOrderEntity(
        @Id String orderId,
        String orderNumber,
        String tenantId,
        String organizationId,
        String userId,
        String cartId,
        String checkoutCorrelationId,
        String addressId,
        String countryCode,
        long regionalPolicyVersion,
        String policyCurrency,
        String status,
        String paymentStatus,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        long version,
        Instant createdAt,
        Instant updatedAt) {}

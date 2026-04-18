package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("payment_records")
public record PaymentRecordEntity(
        @Id String paymentRecordId,
        String orderId,
        String organizationId,

        String paymentReference,
        BigDecimal amount,
        String method,
        String supportReference,
        String status,
        Instant receivedAt,
        Instant createdAt,
        Instant updatedAt) {}

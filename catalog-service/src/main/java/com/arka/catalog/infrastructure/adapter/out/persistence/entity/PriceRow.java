package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("prices")
public record PriceRow(
        @Id
        @Column("price_id") String priceId,
        @Column("tenant_id") String tenantId,
        @Column("variant_id") String variantId,
        @Column("price_type") String priceType,
        @Column("currency") String currency,
        @Column("amount") BigDecimal amount,
        @Column("effective_from") Instant effectiveFrom,
        @Column("effective_until") Instant effectiveUntil,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("replenishment_projections")
public record ReplenishmentProjectionRow(
        @Id
        @Column("projection_id") String projectionId,
        @Column("tenant_id") String tenantId,
        @Column("period") String period,
        @Column("sku") String sku,
        @Column("available_qty") BigDecimal availableQty,
        @Column("reorder_point") BigDecimal reorderPoint,
        @Column("coverage_days") BigDecimal coverageDays,
        @Column("risk_level") String riskLevel,
        @Column("version") Long version,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

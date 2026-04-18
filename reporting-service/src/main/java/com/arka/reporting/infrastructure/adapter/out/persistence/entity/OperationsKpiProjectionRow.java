package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("operations_kpi_projections")
public record OperationsKpiProjectionRow(
        @Id
        @Column("projection_id") String projectionId,
        @Column("organization_id") String organizationId,
        @Column("period") String period,
        @Column("kpi_name") String kpiName,
        @Column("kpi_value") BigDecimal kpiValue,
        @Column("version") Long version,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

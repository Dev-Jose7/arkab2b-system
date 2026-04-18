package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("brands")
public record BrandRow(
        @Id
        @Column("brand_id") String brandId,
        @Column("tenant_id") String tenantId,
        @Column("brand_code") String brandCode,
        @Column("brand_name") String brandName,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

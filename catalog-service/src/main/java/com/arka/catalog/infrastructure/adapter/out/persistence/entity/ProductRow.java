package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("products")
public record ProductRow(
        @Id
        @Column("product_id") String productId,
        @Column("tenant_id") String tenantId,
        @Column("product_code") String productCode,
        @Column("product_name") String productName,
        @Column("description") String description,
        @Column("brand_id") String brandId,
        @Column("category_id") String categoryId,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

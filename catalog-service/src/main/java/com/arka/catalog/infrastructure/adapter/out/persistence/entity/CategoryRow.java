package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("categories")
public record CategoryRow(
        @Id
        @Column("category_id") String categoryId,
        @Column("tenant_id") String tenantId,
        @Column("category_code") String categoryCode,
        @Column("category_name") String categoryName,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

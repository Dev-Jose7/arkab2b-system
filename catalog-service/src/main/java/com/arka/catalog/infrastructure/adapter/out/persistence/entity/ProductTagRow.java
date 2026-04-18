package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("product_tags")
public record ProductTagRow(
        @Id
        @Column("tag_id") String tagId,
        @Column("organization_id") String organizationId,
        @Column("product_id") String productId,
        @Column("tag_code") String tagCode,
        @Column("tag_value") String tagValue,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

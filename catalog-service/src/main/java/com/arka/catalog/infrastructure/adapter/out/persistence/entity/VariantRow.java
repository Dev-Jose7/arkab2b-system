package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("variants")
public record VariantRow(
        @Id
        @Column("variant_id") String variantId,
        @Column("organization_id") String organizationId,
        @Column("product_id") String productId,
        @Column("sku") String sku,
        @Column("variant_name") String variantName,
        @Column("description") String description,
        @Column("status") String status,
        @Column("sellable_from") Instant sellableFrom,
        @Column("sellable_until") Instant sellableUntil,
        @Column("weight_grams") Integer weightGrams,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

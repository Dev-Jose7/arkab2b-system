package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("variant_attributes")
public record VariantAttributeRow(
        @Id
        @Column("attribute_id") String attributeId,
        @Column("organization_id") String organizationId,
        @Column("variant_id") String variantId,
        @Column("attribute_code") String attributeCode,
        @Column("attribute_value") String attributeValue,
        @Column("normalized_value") String normalizedValue,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}

package com.arka.directory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("organization_contact")
public record OrganizationContactRow(
        @Id @Column("contact_id") String contactId,
        @Column("organization_id") String organizationId,
        @Column("contact_type") String contactType,
        @Column("label") String label,
        @Column("value_normalized") String valueNormalized,
        @Column("value_masked") String valueMasked,
        @Column("is_primary") Boolean isPrimary,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}

package com.arka.directory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("address")
public record AddressRow(
        @Id @Column("address_id") String addressId,
        @Column("organization_id") String organizationId,
        @Column("address_type") String addressType,
        @Column("alias") String alias,
        @Column("line1") String line1,
        @Column("line2") String line2,
        @Column("city") String city,
        @Column("state_region") String stateRegion,
        @Column("postal_code") String postalCode,
        @Column("country_code") String countryCode,
        @Column("reference") String reference,
        @Column("latitude") Double latitude,
        @Column("longitude") Double longitude,
        @Column("is_default") Boolean isDefault,
        @Column("status") String status,
        @Column("validation_status") String validationStatus,
        @Column("validated_at") Instant validatedAt,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}

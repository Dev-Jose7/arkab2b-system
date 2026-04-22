package com.arka.directory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("organization")
public record OrganizationRow(
        @Id @Column("organization_id") String organizationId,
        @Column("legal_name") String legalName,
        @Column("trade_name") String tradeName,
        @Column("country_code") String countryCode,
        @Column("currency_code") String currencyCode,
        @Column("timezone") String timezone,
        @Column("segment_tier") String segmentTier,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}

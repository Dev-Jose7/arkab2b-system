package com.arka.directory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("organization_legal_profile")
public record OrganizationLegalProfileRow(
        @Id @Column("legal_profile_id") String legalProfileId,
        @Column("organization_id") String organizationId,
        @Column("tax_id_type") String taxIdType,
        @Column("tax_id") String taxId,
        @Column("fiscal_regime") String fiscalRegime,
        @Column("legal_representative") String legalRepresentative,
        @Column("country_code") String countryCode,
        @Column("verification_status") String verificationStatus,
        @Column("verified_at") Instant verifiedAt,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}

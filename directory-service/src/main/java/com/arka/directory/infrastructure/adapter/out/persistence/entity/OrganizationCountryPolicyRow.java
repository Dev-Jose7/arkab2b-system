package com.arka.directory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("organization_country_policy")
public record OrganizationCountryPolicyRow(
        @Id @Column("policy_id") String policyId,
        @Column("organization_id") String organizationId,
        @Column("country_code") String countryCode,
        @Column("policy_version") Long policyVersion,
        @Column("currency_code") String currencyCode,
        @Column("week_starts_on") String weekStartsOn,
        @Column("weekly_cutoff_local_time") String weeklyCutoffLocalTime,
        @Column("timezone") String timezone,
        @Column("reporting_retention_days") Integer reportingRetentionDays,
        @Column("requires_verified_address") Boolean requiresVerifiedAddress,
        @Column("effective_from") Instant effectiveFrom,
        @Column("effective_to") Instant effectiveTo,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}

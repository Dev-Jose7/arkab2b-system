package com.arka.directory.domain.organizationcontext.entity;

import com.arka.directory.domain.organizationcontext.enumtype.OrganizationLegalProfileStatus;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record OrganizationLegalProfile(
        String legalProfileId,
        String organizationId,
        String taxIdType,
        String taxId,
        String fiscalRegime,
        String legalRepresentative,
        String countryCode,
        OrganizationLegalProfileStatus verificationStatus,
        Instant verifiedAt,
        Instant createdAt,
        Instant updatedAt) {

    public OrganizationLegalProfile {
        requireNotBlank(legalProfileId, "legalProfileId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(taxIdType, "taxIdType");
        taxId = normalizeTaxId(taxId);
        requireNotBlank(countryCode, "countryCode");
        if (verificationStatus == null) {
            throw new DomainInvariantViolationException("Legal profile status is required");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public OrganizationLegalProfile upsert(
            String taxIdType,
            String taxId,
            String fiscalRegime,
            String legalRepresentative,
            String countryCode,
            OrganizationLegalProfileStatus verificationStatus,
            Instant updatedAt) {
        return new OrganizationLegalProfile(
                legalProfileId,
                organizationId,
                taxIdType,
                taxId,
                fiscalRegime,
                legalRepresentative,
                countryCode,
                verificationStatus,
                verificationStatus != null && verificationStatus.isVerified() ? updatedAt : null,
                createdAt,
                updatedAt);
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }

    private static String normalizeTaxId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("taxId is required");
        }
        return value.trim().toUpperCase();
    }
}

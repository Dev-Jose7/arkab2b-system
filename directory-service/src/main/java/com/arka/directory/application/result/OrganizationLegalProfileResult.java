package com.arka.directory.application.result;

import java.time.Instant;

public record OrganizationLegalProfileResult(
        String legalProfileId,
        String organizationId,
        String taxIdType,
        String taxId,
        String fiscalRegime,
        String legalRepresentative,
        String countryCode,
        String verificationStatus,
        Instant verifiedAt,
        Instant createdAt,
        Instant updatedAt) {}

package com.arka.directory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record OrganizationLegalProfileResponse(
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

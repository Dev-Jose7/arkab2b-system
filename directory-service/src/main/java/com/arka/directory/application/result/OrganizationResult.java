package com.arka.directory.application.result;

import java.time.Instant;

public record OrganizationResult(
        String organizationId,
        String legalName,
        String tradeName,
        String countryCode,
        String currencyCode,
        String timezone,
        String segmentTier,
        String status,
        Instant createdAt,
        Instant updatedAt) {}

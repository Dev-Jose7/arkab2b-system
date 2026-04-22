package com.arka.directory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record OrganizationResponse(
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

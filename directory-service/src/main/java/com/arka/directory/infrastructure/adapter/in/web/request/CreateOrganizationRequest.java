package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 255) String legalName,
        @Size(max = 255) String tradeName,
        @NotBlank @Size(min = 2, max = 2) String countryCode,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @NotBlank @Size(max = 100) String timezone,
        @Size(max = 80) String segmentTier) {}

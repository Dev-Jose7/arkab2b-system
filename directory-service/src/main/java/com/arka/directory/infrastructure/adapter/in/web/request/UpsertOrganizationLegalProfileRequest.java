package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertOrganizationLegalProfileRequest(
        @NotBlank @Size(max = 30) String taxIdType,
        @NotBlank @Size(max = 100) String taxId,
        @Size(max = 120) String fiscalRegime,
        @Size(max = 255) String legalRepresentative,
        @NotBlank @Size(min = 2, max = 2) String countryCode,
        @Size(max = 30) String verificationStatus) {}

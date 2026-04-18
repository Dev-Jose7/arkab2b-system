package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ConfigureRegionalPolicyRequest(
        @NotBlank @Size(min = 2, max = 2) String countryCode,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @NotBlank @Size(max = 20) String weekStartsOn,
        @NotBlank @Size(max = 8) String weeklyCutoffLocalTime,
        @NotBlank @Size(max = 100) String timezone,
        @NotNull Integer reportingRetentionDays,
        Boolean requiresVerifiedAddress,
        Instant effectiveFrom) {}

package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertAddressRequest(
        @NotBlank @Size(max = 30) String addressType,
        @Size(max = 120) String alias,
        @NotBlank @Size(max = 255) String line1,
        @Size(max = 255) String line2,
        @NotBlank @Size(max = 120) String city,
        @Size(max = 120) String stateRegion,
        @NotBlank @Size(max = 30) String postalCode,
        @NotBlank @Size(min = 2, max = 2) String countryCode,
        @Size(max = 255) String reference,
        Double latitude,
        Double longitude,
        Boolean isDefault,
        @Size(max = 30) String status,
        @Size(max = 30) String validationStatus) {}

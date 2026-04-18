package com.arka.directory.application.result;

import java.time.Instant;

public record AddressResult(
        String addressId,
        String organizationId,
        String addressType,
        String alias,
        String line1,
        String line2,
        String city,
        String stateRegion,
        String postalCode,
        String countryCode,
        String reference,
        Double latitude,
        Double longitude,
        boolean isDefault,
        String status,
        String validationStatus,
        Instant validatedAt,
        Instant createdAt,
        Instant updatedAt) {}

package com.arka.directory.application.command;

public record UpsertAddressCommand(
        String organizationId,
        String addressId,
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
        Boolean isDefault,
        String status,
        String validationStatus,
        String actorUserId,
        String actorOrganizationId) {}

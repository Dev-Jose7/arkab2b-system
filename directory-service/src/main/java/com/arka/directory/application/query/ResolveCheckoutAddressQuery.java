package com.arka.directory.application.query;

public record ResolveCheckoutAddressQuery(
        String organizationId,
        String addressId,
        String countryCode,
        String actorUserId,
        String actorOrganizationId) {}

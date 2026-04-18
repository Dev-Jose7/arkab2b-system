package com.arka.directory.application.query;

public record GetActiveCountryPolicyQuery(
        String organizationId,
        String countryCode,
        String actorUserId,
        String actorOrganizationId) {}

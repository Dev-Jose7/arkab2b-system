package com.arka.directory.application.query;

public record ListOrganizationAddressesQuery(
        String organizationId,
        String actorUserId,
        String actorOrganizationId) {}

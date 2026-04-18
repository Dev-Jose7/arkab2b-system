package com.arka.directory.application.query;

public record ListOrganizationContactsQuery(
        String organizationId,
        String actorUserId,
        String actorOrganizationId) {}

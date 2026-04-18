package com.arka.directory.application.query;

public record GetOrganizationQuery(
        String organizationId,
        String actorUserId,
        String actorOrganizationId) {}

package com.arka.directory.application.query;

public record GetOrganizationProfileQuery(
        String organizationId,
        String actorUserId,
        String actorOrganizationId) {}

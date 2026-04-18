package com.arka.directory.application.query;

public record GetDirectoryAuditQuery(
        String organizationId,
        int limit,
        String actorUserId,
        String actorOrganizationId) {}

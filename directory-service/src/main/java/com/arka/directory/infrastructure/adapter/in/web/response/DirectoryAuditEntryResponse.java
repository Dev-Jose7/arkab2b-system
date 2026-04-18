package com.arka.directory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record DirectoryAuditEntryResponse(
        String auditId,
        String organizationId,
        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {}

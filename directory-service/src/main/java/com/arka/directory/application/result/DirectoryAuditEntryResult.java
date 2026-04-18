package com.arka.directory.application.result;

import java.time.Instant;

public record DirectoryAuditEntryResult(
        String auditId,
        String organizationId,
        String actorUserId,
        String actionType,
        String targetType,
        String targetId,
        String outcome,
        String payload,
        Instant createdAt) {}

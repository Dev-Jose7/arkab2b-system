package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record ReportArtifactResponse(
        String artifactId,
        String executionId,
        String organizationId,
        String weekId,
        String reportType,
        String format,
        String locationRef,
        String contentHash,
        long sizeBytes,
        Instant createdAt,
        Instant updatedAt) {}

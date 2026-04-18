package com.arka.reporting.domain.weeklyreportexecution.entity;

import java.time.Instant;

public record ReportArtifact(
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
        Instant updatedAt) {
}

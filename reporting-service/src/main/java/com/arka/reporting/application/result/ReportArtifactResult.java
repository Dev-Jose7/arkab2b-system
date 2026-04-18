package com.arka.reporting.application.result;

import java.time.Instant;

public record ReportArtifactResult(
        String artifactId,
        String executionId,
        String tenantId,
        String weekId,
        String reportType,
        String format,
        String locationRef,
        String contentHash,
        long sizeBytes,
        Instant createdAt,
        Instant updatedAt) {
}

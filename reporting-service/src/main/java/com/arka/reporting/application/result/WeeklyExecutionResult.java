package com.arka.reporting.application.result;

import java.time.Instant;

public record WeeklyExecutionResult(
        String executionId,
        String tenantId,
        String weekId,
        String reportType,
        String status,
        String errorCode,
        String errorMessage,
        String completionArtifactRef,
        long version,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt,
        Instant updatedAt) {
}

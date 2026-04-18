package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record WeeklyExecutionResponse(
        String executionId,
        String organizationId,
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
        Instant updatedAt) {}

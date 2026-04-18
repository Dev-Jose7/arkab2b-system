package com.arka.reporting.application.command;

public record GenerateReportArtifactCommand(
        String tenantId,
        String actorId,
        String executionId,
        String weekId,
        String reportType,
        String format,
        String payload,
        String idempotencyKey) {
}

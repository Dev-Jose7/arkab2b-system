package com.arka.reporting.application.command;

public record GenerateReportArtifactCommand(
        String organizationId,
        String actorId,
        String executionId,
        String weekId,
        String reportType,
        String format,
        String payload,
        String idempotencyKey) {
}

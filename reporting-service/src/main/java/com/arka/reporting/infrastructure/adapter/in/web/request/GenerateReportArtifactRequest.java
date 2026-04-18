package com.arka.reporting.infrastructure.adapter.in.web.request;

public record GenerateReportArtifactRequest(
        String weekId,
        String reportType,
        String format,
        String payload,
        String idempotencyKey) {
}

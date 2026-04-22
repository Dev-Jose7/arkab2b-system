package com.arka.reporting.infrastructure.adapter.in.web.response;

public record GeneratedBusinessReportResponse(
        String message,
        String reportType,
        WeeklyExecutionResponse execution,
        ReportArtifactResponse artifact) {
}

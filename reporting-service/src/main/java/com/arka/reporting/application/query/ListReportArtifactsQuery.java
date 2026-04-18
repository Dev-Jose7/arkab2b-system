package com.arka.reporting.application.query;

public record ListReportArtifactsQuery(
        String tenantId,
        String weekId,
        String reportType,
        int page,
        int size) {
}

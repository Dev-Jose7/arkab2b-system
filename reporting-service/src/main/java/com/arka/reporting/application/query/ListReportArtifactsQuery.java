package com.arka.reporting.application.query;

public record ListReportArtifactsQuery(
        String organizationId,
        String weekId,
        String reportType,
        int page,
        int size) {
}

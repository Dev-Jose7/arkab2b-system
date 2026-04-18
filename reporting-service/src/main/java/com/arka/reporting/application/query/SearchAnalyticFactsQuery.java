package com.arka.reporting.application.query;

public record SearchAnalyticFactsQuery(
        String organizationId,
        String eventType,
        String factType,
        String period,
        String status,
        int page,
        int size) {
}

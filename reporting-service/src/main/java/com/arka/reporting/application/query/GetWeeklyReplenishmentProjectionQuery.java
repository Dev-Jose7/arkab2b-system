package com.arka.reporting.application.query;

public record GetWeeklyReplenishmentProjectionQuery(
        String tenantId,
        String period,
        String sku,
        int page,
        int size) {
}

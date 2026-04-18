package com.arka.reporting.application.query;

public record GetWeeklyReplenishmentProjectionQuery(
        String organizationId,
        String period,
        String sku,
        int page,
        int size) {
}

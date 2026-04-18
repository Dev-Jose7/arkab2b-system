package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record ReportingMetricsResponse(
        BigDecimal weeklySalesTotal,
        BigDecimal weeklyCollectionRate,
        long replenishmentHighRiskCount,
        BigDecimal notificationEffectiveness,
        long consumerLag) {
}

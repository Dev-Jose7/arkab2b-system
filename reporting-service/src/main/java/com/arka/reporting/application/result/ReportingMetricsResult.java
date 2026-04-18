package com.arka.reporting.application.result;

import java.math.BigDecimal;

public record ReportingMetricsResult(
        BigDecimal weeklySalesTotal,
        BigDecimal weeklyCollectionRate,
        long replenishmentHighRiskCount,
        BigDecimal notificationEffectiveness,
        long consumerLag) {
}

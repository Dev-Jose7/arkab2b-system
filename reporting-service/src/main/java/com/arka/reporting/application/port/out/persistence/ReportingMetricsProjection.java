package com.arka.reporting.application.port.out.persistence;

import java.math.BigDecimal;

public record ReportingMetricsProjection(
        BigDecimal weeklySalesTotal,
        BigDecimal weeklyCollectionRate,
        long replenishmentHighRiskCount,
        BigDecimal notificationEffectiveness,
        long consumerLag) {
}

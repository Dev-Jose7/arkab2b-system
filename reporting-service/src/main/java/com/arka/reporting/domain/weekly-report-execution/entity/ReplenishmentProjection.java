package com.arka.reporting.domain.weeklyreportexecution.entity;

import com.arka.reporting.domain.weeklyreportexecution.enumtype.RiskLevel;
import java.math.BigDecimal;
import java.time.Instant;

public record ReplenishmentProjection(
        String projectionId,
        String organizationId,
        String period,
        String sku,
        BigDecimal availableQty,
        BigDecimal reorderPoint,
        BigDecimal coverageDays,
        RiskLevel riskLevel,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}

package com.arka.reporting.domain.weeklyreportexecution.entity;

import java.math.BigDecimal;
import java.time.Instant;

public record OperationsKpiProjection(
        String projectionId,
        String organizationId,
        String period,
        String kpiName,
        BigDecimal kpiValue,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}

package com.arka.reporting.application.result;

import java.math.BigDecimal;

public record OperationsKpiResult(
        String projectionId,
        String organizationId,
        String period,
        String kpiName,
        BigDecimal kpiValue) {
}

package com.arka.reporting.application.result;

import java.math.BigDecimal;

public record ReplenishmentProjectionResult(
        String projectionId,
        String organizationId,
        String period,
        String sku,
        BigDecimal availableQty,
        BigDecimal reorderPoint,
        BigDecimal coverageDays,
        String riskLevel) {
}

package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record ReplenishmentProjectionResponse(
        String projectionId,
        String tenantId,
        String period,
        String sku,
        BigDecimal availableQty,
        BigDecimal reorderPoint,
        BigDecimal coverageDays,
        String riskLevel) {
}

package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record SalesProjectionResponse(
        String projectionId,
        String tenantId,
        String period,
        BigDecimal totalSales,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        long confirmedOrders,
        BigDecimal averageTicket) {
}

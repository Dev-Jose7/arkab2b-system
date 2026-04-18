package com.arka.reporting.application.result;

import java.math.BigDecimal;

public record SalesProjectionResult(
        String projectionId,
        String organizationId,
        String period,
        BigDecimal totalSales,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        long confirmedOrders,
        BigDecimal averageTicket) {
}

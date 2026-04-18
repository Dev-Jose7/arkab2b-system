package com.arka.reporting.domain.weeklyreportexecution.valueobject;

import java.math.BigDecimal;

public record WeeklySalesReport(
        String organizationId,
        String weekId,
        BigDecimal totalSales,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        long confirmedOrders,
        BigDecimal averageTicket) {
}

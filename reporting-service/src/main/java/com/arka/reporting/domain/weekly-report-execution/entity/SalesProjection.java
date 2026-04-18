package com.arka.reporting.domain.weeklyreportexecution.entity;

import java.math.BigDecimal;
import java.time.Instant;

public record SalesProjection(
        String projectionId,
        String organizationId,
        String period,
        BigDecimal totalSales,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        long confirmedOrders,
        BigDecimal averageTicket,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}

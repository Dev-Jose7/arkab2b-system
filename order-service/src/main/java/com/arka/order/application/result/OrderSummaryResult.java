package com.arka.order.application.result;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResult(
        String orderId,
        String orderNumber,
        String status,
        String financialStatus,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        Instant createdAt,
        Instant updatedAt) {}

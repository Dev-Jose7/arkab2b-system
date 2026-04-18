package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        String orderId,
        String orderNumber,
        String status,
        String financialStatus,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        Instant createdAt,
        Instant updatedAt) {}

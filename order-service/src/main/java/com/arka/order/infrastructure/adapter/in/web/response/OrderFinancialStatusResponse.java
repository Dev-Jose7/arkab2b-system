package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record OrderFinancialStatusResponse(
        String orderId,
        String financialStatus,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount) {}

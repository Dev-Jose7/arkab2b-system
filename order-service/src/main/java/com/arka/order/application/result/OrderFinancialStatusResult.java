package com.arka.order.application.result;

import java.math.BigDecimal;

public record OrderFinancialStatusResult(
        String orderId,
        String financialStatus,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount) {}

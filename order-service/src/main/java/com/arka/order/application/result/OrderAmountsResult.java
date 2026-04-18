package com.arka.order.application.result;

import java.math.BigDecimal;

public record OrderAmountsResult(
        String orderId,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount) {}

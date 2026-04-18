package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record OrderAmountsResponse(
        String orderId,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount) {}

package com.arka.order.application.result;

import java.math.BigDecimal;

public record OrderLineResult(
        String orderLineId,
        String variantId,
        String sku,
        int qty,
        BigDecimal unitPrice,
        String currency,
        String reservationId,
        boolean reservationConfirmed,
        BigDecimal lineTotal) {}

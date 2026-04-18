package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record OrderLineResponse(
        String orderLineId,
        String variantId,
        String sku,
        int qty,
        BigDecimal unitPrice,
        String currency,
        String reservationId,
        boolean reservationConfirmed,
        BigDecimal lineTotal) {}

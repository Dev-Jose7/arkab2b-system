package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;

public record CartItemResponse(
        String cartItemId,
        String variantId,
        String sku,
        int qty,
        BigDecimal unitPrice,
        String currency,
        String reservationId,
        boolean reservationConfirmed,
        BigDecimal lineSubtotal,
        Instant createdAt,
        Instant updatedAt) {}

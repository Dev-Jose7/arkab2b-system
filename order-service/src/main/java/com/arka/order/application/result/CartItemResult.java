package com.arka.order.application.result;

import java.math.BigDecimal;
import java.time.Instant;

public record CartItemResult(
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

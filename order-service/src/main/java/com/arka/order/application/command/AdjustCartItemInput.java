package com.arka.order.application.command;

import java.math.BigDecimal;

public record AdjustCartItemInput(
        String cartItemId,
        String operation,
        String variantId,
        String sku,
        Integer qty,
        BigDecimal unitPrice,
        String currency,
        String reservationId,
        Boolean reservationConfirmed) {}

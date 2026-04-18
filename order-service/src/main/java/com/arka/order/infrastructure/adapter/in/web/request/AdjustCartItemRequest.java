package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AdjustCartItemRequest(
        String cartItemId,
        String operation,
        String variantId,
        String sku,
        @Positive Integer qty,
        BigDecimal unitPrice,
        String currency,
        String reservationId,
        Boolean reservationConfirmed) {

    @Override
    public String operation() {
        return operation == null ? "UPSERT" : operation;
    }
}

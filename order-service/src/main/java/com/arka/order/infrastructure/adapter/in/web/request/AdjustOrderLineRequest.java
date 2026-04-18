package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AdjustOrderLineRequest(
        String orderLineId,
        @NotBlank String variantId,
        @NotBlank String sku,
        @Positive Integer qty,
        BigDecimal unitPrice,
        String currency,
        @NotBlank String reservationId,
        Boolean reservationConfirmed) {}

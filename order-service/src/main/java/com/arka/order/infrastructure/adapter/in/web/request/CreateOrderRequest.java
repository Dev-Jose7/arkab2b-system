package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank String cartId,
        @NotBlank String checkoutCorrelationId,
        String userId) {}

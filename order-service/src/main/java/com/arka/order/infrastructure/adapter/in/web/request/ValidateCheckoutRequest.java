package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateCheckoutRequest(
        @NotBlank String checkoutCorrelationId,
        @NotBlank String addressId,
        @NotBlank String countryCode) {}

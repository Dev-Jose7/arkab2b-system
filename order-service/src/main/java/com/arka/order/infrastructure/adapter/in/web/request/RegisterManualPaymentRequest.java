package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record RegisterManualPaymentRequest(
        @NotBlank String paymentReference,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String method,
        @NotBlank String supportReference,
        Instant receivedAt) {}

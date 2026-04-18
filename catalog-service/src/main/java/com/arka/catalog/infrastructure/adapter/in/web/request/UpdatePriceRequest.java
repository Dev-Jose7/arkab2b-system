package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record UpdatePriceRequest(
        @NotNull(message = "amount es obligatorio")
        @DecimalMin(value = "0.0001", message = "amount debe ser mayor que cero")
        BigDecimal amount,
        @NotBlank(message = "currency es obligatoria")
        @Size(min = 3, max = 3, message = "currency debe tener 3 caracteres")
        String currency,
        @NotBlank(message = "priceType es obligatorio")
        String priceType,
        @NotNull(message = "effectiveFrom es obligatorio")
        Instant effectiveFrom,
        Instant effectiveUntil,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

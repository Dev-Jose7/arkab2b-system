package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record SchedulePriceActivationRequest(
        @NotNull(message = "executeAfter es obligatorio")
        Instant executeAfter,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

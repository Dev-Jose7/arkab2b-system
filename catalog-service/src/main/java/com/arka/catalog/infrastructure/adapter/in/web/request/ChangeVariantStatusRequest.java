package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ChangeVariantStatusRequest(
        @NotBlank(message = "targetStatus es obligatorio")
        String targetStatus,
        Instant sellableFrom,
        Instant sellableUntil,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

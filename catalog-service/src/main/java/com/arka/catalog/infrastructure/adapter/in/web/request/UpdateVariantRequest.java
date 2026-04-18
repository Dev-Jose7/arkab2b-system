package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateVariantRequest(
        @NotBlank(message = "name es obligatorio")
        @Size(max = 200, message = "name maximo 200 caracteres")
        String name,
        @Size(max = 2000, message = "description maximo 2000 caracteres")
        String description,
        Integer weightGrams,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

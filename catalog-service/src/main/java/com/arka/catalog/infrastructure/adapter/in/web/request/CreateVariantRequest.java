package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateVariantRequest(
        @NotBlank(message = "sku es obligatorio")
        @Size(max = 80, message = "sku maximo 80 caracteres")
        String sku,
        @NotBlank(message = "name es obligatorio")
        @Size(max = 200, message = "name maximo 200 caracteres")
        String name,
        @Size(max = 2000, message = "description maximo 2000 caracteres")
        String description,
        Integer weightGrams,
        List<@Valid VariantAttributeRequest> attributes,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

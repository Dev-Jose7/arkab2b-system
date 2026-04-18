package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCatalogOfferRequest(
        @NotBlank(message = "variantId es obligatorio")
        String variantId,
        @NotBlank(message = "priceId es obligatorio")
        String priceId,
        @Size(max = 150, message = "regionalPolicyReference maximo 150 caracteres")
        String regionalPolicyReference,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

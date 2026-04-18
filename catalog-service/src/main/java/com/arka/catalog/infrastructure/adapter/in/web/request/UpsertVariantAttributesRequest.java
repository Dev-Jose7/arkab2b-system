package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpsertVariantAttributesRequest(
        @NotEmpty(message = "attributes debe contener al menos un atributo")
        List<@Valid VariantAttributeRequest> attributes,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

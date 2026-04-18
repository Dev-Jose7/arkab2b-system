package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VariantAttributeRequest(
        @NotBlank(message = "attributeCode es obligatorio")
        @Size(max = 100, message = "attributeCode maximo 100 caracteres")
        String attributeCode,
        @NotBlank(message = "value es obligatorio")
        @Size(max = 255, message = "value maximo 255 caracteres")
        String value,
        @Size(max = 255, message = "normalizedValue maximo 255 caracteres")
        String normalizedValue) {
}

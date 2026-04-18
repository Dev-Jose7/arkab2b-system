package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateProductRequest(
        @NotBlank(message = "productCode es obligatorio")
        @Size(max = 64, message = "productCode maximo 64 caracteres")
        String productCode,
        @NotBlank(message = "name es obligatorio")
        @Size(max = 200, message = "name maximo 200 caracteres")
        String name,
        @Size(max = 2000, message = "description maximo 2000 caracteres")
        String description,
        @NotBlank(message = "brandId es obligatorio")
        String brandId,
        @NotBlank(message = "categoryId es obligatorio")
        String categoryId,
        List<@Size(max = 128, message = "tag maxima longitud 128") String> tags,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {
}

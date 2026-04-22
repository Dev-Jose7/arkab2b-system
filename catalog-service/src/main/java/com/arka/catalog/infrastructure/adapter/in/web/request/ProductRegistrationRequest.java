package com.arka.catalog.infrastructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductRegistrationRequest(
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
        @NotNull(message = "variant es obligatoria")
        @Valid VariantRegistrationRequest variant,
        @NotNull(message = "price es obligatoria")
        @Valid PriceRegistrationRequest price,
        @NotNull(message = "stock es obligatorio")
        @Valid StockRegistrationRequest stock,
        @Size(max = 150, message = "regionalPolicyReference maximo 150 caracteres")
        String regionalPolicyReference,
        @Size(max = 128, message = "idempotencyKey maximo 128 caracteres")
        String idempotencyKey) {

    public record VariantRegistrationRequest(
            @NotBlank(message = "sku es obligatorio")
            @Size(max = 80, message = "sku maximo 80 caracteres")
            String sku,
            @NotBlank(message = "name es obligatorio")
            @Size(max = 200, message = "name maximo 200 caracteres")
            String name,
            @Size(max = 2000, message = "description maximo 2000 caracteres")
            String description,
            Integer weightGrams,
            @NotEmpty(message = "attributes es obligatorio")
            List<@Valid VariantAttributeRegistrationRequest> attributes) {
    }

    public record VariantAttributeRegistrationRequest(
            @NotBlank(message = "attributeCode es obligatorio")
            @Size(max = 100, message = "attributeCode maximo 100 caracteres")
            String attributeCode,
            @NotBlank(message = "value es obligatorio")
            @Size(max = 255, message = "value maximo 255 caracteres")
            String value,
            @Size(max = 255, message = "normalizedValue maximo 255 caracteres")
            String normalizedValue) {
    }

    public record PriceRegistrationRequest(
            @NotNull(message = "amount es obligatorio")
            @DecimalMin(value = "0.0001", message = "amount debe ser mayor que cero")
            BigDecimal amount,
            @NotBlank(message = "currency es obligatoria")
            @Size(min = 3, max = 3, message = "currency debe tener 3 caracteres")
            String currency,
            @NotBlank(message = "priceType es obligatorio")
            String priceType,
            Instant effectiveFrom,
            Instant effectiveUntil) {
    }

    public record StockRegistrationRequest(
            @NotBlank(message = "warehouseId es obligatorio")
            String warehouseId,
            @NotNull(message = "initialPhysicalQty es obligatorio")
            @Min(value = 0, message = "initialPhysicalQty debe ser >= 0")
            Integer initialPhysicalQty,
            @NotNull(message = "reorderPoint es obligatorio")
            @Min(value = 0, message = "reorderPoint debe ser >= 0")
            Integer reorderPoint,
            @NotNull(message = "safetyStock es obligatorio")
            @Min(value = 0, message = "safetyStock debe ser >= 0")
            Integer safetyStock) {
    }
}

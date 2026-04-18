package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InitializeStockItemRequest(
        @NotBlank(message = "warehouseId is required")
        String warehouseId,

        @NotBlank(message = "sku is required")
        String sku,

        @NotNull(message = "initialPhysicalQty is required")
        @Min(value = 0, message = "initialPhysicalQty must be >= 0")
        Integer initialPhysicalQty,

        @NotNull(message = "reorderPoint is required")
        @Min(value = 0, message = "reorderPoint must be >= 0")
        Integer reorderPoint,

        @NotNull(message = "safetyStock is required")
        @Min(value = 0, message = "safetyStock must be >= 0")
        Integer safetyStock) {}

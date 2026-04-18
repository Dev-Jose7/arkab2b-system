package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
        @NotBlank(message = "warehouseCode is required")
        @Size(max = 60, message = "warehouseCode max length is 60")
        String warehouseCode,

        @NotBlank(message = "warehouseName is required")
        @Size(max = 255, message = "warehouseName max length is 255")
        String warehouseName,

        @NotBlank(message = "countryCode is required")
        @Size(min = 2, max = 2, message = "countryCode must have 2 chars")
        String countryCode) {}

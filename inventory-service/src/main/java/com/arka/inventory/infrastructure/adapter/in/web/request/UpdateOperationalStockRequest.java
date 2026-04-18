package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateOperationalStockRequest(
        @NotNull(message = "deltaQty is required")
        Integer deltaQty,

        @NotBlank(message = "reason is required")
        String reason) {}

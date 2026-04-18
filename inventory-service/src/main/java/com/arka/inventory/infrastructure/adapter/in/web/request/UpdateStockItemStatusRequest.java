package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateStockItemStatusRequest(
        @NotBlank(message = "targetStatus is required")
        String targetStatus,

        String reason) {}

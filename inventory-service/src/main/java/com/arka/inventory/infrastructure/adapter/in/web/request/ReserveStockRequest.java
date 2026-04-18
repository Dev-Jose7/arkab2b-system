package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ReserveStockRequest(
        @NotBlank(message = "cartId is required")
        String cartId,

        @NotNull(message = "qty is required")
        @Min(value = 1, message = "qty must be > 0")
        Integer qty,

        Instant expiresAt) {}

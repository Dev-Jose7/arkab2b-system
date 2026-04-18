package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Min;

public record ExpireReservationsRequest(
        @Min(value = 1, message = "batchSize must be > 0")
        Integer batchSize) {}

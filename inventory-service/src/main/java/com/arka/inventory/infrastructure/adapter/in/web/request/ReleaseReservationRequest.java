package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ReleaseReservationRequest(
        @NotBlank(message = "reason is required")
        String reason) {}

package com.arka.inventory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmReservationRequest(
        @NotBlank(message = "orderId is required")
        String orderId) {}

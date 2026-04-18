package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @NotBlank String targetStatus,
        String reason) {}

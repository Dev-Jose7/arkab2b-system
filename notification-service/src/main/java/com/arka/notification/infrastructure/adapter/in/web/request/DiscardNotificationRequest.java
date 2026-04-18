package com.arka.notification.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record DiscardNotificationRequest(
        @NotBlank(message = "reason es obligatorio") String reason,
        String idempotencyKey) {
}

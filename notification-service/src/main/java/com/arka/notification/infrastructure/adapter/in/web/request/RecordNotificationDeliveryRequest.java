package com.arka.notification.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record RecordNotificationDeliveryRequest(
        String attemptId,
        @NotBlank(message = "providerCode es obligatorio") String providerCode,
        @NotBlank(message = "providerRef es obligatorio") String providerRef,
        String idempotencyKey) {
}

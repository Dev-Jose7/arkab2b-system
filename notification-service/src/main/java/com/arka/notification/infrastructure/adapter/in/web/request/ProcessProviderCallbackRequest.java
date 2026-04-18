package com.arka.notification.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ProcessProviderCallbackRequest(
        @NotBlank(message = "notificationId es obligatorio") String notificationId,
        @NotBlank(message = "providerCode es obligatorio") String providerCode,
        @NotBlank(message = "providerRef es obligatorio") String providerRef,
        @NotBlank(message = "callbackEventId es obligatorio") String callbackEventId,
        @NotBlank(message = "callbackStatus es obligatorio") String callbackStatus,
        String payload,
        String idempotencyKey) {
}

package com.arka.notification.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record EmitNotificationRequest(
        @NotBlank(message = "sourceEventId es obligatorio") String sourceEventId,
        @NotBlank(message = "sourceEventType es obligatorio") String sourceEventType,
        @NotBlank(message = "recipientRef es obligatorio") String recipientRef,
        @NotBlank(message = "channel es obligatorio") String channel,
        String payloadJson,
        String traceId,
        String correlationId,
        String idempotencyKey) {
}

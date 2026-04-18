package com.arka.notification.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ReprocessNotificationDlqRequest(
        @NotBlank(message = "dlqEventId es obligatorio") String dlqEventId,
        @NotBlank(message = "consumerName es obligatorio") String consumerName,
        String idempotencyKey) {
}

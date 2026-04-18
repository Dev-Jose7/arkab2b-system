package com.arka.reporting.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ReprocessReportingDlqRequest(
        @NotBlank(message = "dlqEventId es obligatorio") String dlqEventId,
        String consumerName,
        @NotBlank(message = "factId es obligatorio") String factId,
        String idempotencyKey) {
}

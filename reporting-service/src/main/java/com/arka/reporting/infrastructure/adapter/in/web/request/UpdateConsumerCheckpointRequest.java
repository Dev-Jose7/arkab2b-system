package com.arka.reporting.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateConsumerCheckpointRequest(
        @NotBlank(message = "consumerName es obligatorio") String consumerName,
        @NotBlank(message = "topic es obligatorio") String topic,
        @Min(value = 0, message = "partition debe ser >= 0") int partition,
        long currentOffset,
        long latestOffset,
        String idempotencyKey) {
}

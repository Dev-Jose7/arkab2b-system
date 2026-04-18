package com.arka.reporting.application.command;

public record UpdateConsumerCheckpointCommand(
        String organizationId,
        String actorId,
        String consumerName,
        String topic,
        int partition,
        long currentOffset,
        long latestOffset,
        String idempotencyKey) {
}

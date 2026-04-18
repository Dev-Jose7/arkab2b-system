package com.arka.reporting.domain.weeklyreportexecution.entity;

import java.time.Instant;

public record ConsumerCheckpoint(
        String checkpointId,
        String organizationId,
        String consumerName,
        String topic,
        int partition,
        long currentOffset,
        long latestOffset,
        long lag,
        Instant updatedAt) {
}

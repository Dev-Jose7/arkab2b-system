package com.arka.reporting.application.port.out.persistence;

import com.arka.reporting.domain.weeklyreportexecution.entity.ConsumerCheckpoint;
import reactor.core.publisher.Mono;

public interface ConsumerCheckpointPersistencePort {

    Mono<ConsumerCheckpoint> upsert(
            String organizationId,
            String consumerName,
            String topic,
            int partition,
            long currentOffset,
            long latestOffset);

    Mono<Long> maxLagByOrganization(String organizationId);
}

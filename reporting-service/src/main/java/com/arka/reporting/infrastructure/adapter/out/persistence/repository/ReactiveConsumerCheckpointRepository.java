package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ConsumerCheckpointRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveConsumerCheckpointRepository extends ReactiveCrudRepository<ConsumerCheckpointRow, String> {

    @Query("""
            SELECT *
            FROM consumer_checkpoints
            WHERE organization_id = :organizationId
              AND consumer_name = :consumerName
              AND topic = :topic
              AND partition = :partition
            """)
    Mono<ConsumerCheckpointRow> findByOrganizationConsumerTopicAndPartition(
            String organizationId,
            String consumerName,
            String topic,
            int partition);

    @Query("SELECT COALESCE(MAX(lag), 0) FROM consumer_checkpoints WHERE organization_id = :organizationId")
    Mono<Long> maxLagByOrganization(String organizationId);
}

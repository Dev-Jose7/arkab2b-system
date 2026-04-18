package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ConsumerCheckpointRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveConsumerCheckpointRepository extends ReactiveCrudRepository<ConsumerCheckpointRow, String> {

    @Query("""
            SELECT *
            FROM consumer_checkpoints
            WHERE tenant_id = :tenantId
              AND consumer_name = :consumerName
              AND topic = :topic
              AND partition = :partition
            """)
    Mono<ConsumerCheckpointRow> findByTenantConsumerTopicAndPartition(
            String tenantId,
            String consumerName,
            String topic,
            int partition);

    @Query("SELECT COALESCE(MAX(lag), 0) FROM consumer_checkpoints WHERE tenant_id = :tenantId")
    Mono<Long> maxLagByTenant(String tenantId);
}

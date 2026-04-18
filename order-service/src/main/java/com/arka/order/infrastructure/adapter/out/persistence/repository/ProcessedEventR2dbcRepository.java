package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.ProcessedEventEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProcessedEventR2dbcRepository extends ReactiveCrudRepository<ProcessedEventEntity, String> {

    @Query("""
            SELECT COUNT(1) > 0
            FROM processed_events
            WHERE event_id = :eventId
              AND consumer_name = :consumerName
            """)
    Mono<Boolean> existsByEventAndConsumer(String eventId, String consumerName);
}

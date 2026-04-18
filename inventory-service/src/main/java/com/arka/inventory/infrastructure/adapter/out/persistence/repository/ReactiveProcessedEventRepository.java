package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.ProcessedEventRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveProcessedEventRepository extends ReactiveCrudRepository<ProcessedEventRow, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM processed_events WHERE event_id = :eventId AND consumer_name = :consumerName)")
    Mono<Boolean> existsByEventAndConsumer(String eventId, String consumerName);
}

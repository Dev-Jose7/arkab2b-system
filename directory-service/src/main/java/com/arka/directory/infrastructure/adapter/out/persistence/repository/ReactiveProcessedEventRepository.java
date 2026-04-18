package com.arka.directory.infrastructure.adapter.out.persistence.repository;

import com.arka.directory.infrastructure.adapter.out.persistence.entity.ProcessedEventRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveProcessedEventRepository extends ReactiveCrudRepository<ProcessedEventRow, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM processed_event WHERE event_id = :eventId AND consumer_name = :consumerName)")
    Mono<Boolean> existsByEventIdAndConsumerName(
            @Param("eventId") String eventId,
            @Param("consumerName") String consumerName);
}

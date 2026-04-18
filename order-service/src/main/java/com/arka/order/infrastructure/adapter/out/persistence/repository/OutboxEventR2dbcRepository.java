package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxEventR2dbcRepository extends ReactiveCrudRepository<OutboxEventEntity, String> {

    @Query("""
            SELECT *
            FROM outbox_events
            WHERE status IN ('PENDING', 'FAILED')
            ORDER BY occurred_at ASC
            LIMIT :limit
            """)
    Flux<OutboxEventEntity> findPending(int limit);

    @Modifying
    @Query("""
            UPDATE outbox_events
               SET status = 'PUBLISHED',
                   published_at = :publishedAt,
                   updated_at = :publishedAt,
                   last_error = NULL
             WHERE event_id = :eventId
            """)
    Mono<Integer> markPublished(String eventId, Instant publishedAt);

    @Modifying
    @Query("""
            UPDATE outbox_events
               SET status = CASE WHEN retry_count + 1 >= :maxRetries THEN 'FAILED' ELSE 'PENDING' END,
                   retry_count = retry_count + 1,
                   last_error = :lastError,
                   updated_at = :failedAt
             WHERE event_id = :eventId
            """)
    Mono<Integer> markFailed(String eventId, String lastError, Instant failedAt, int maxRetries);
}

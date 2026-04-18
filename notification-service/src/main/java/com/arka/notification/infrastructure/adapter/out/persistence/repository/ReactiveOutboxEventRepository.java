package com.arka.notification.infrastructure.adapter.out.persistence.repository;

import com.arka.notification.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveOutboxEventRepository extends ReactiveCrudRepository<OutboxEventRow, String> {

    @Query("""
            SELECT *
            FROM outbox_events
            WHERE status = 'PENDING'
            ORDER BY occurred_at
            LIMIT :limit
            """)
    Flux<OutboxEventRow> findPending(int limit);

    @Query("""
            UPDATE outbox_events
            SET status = 'PUBLISHED',
                published_at = :publishedAt,
                updated_at = :publishedAt
            WHERE event_id = :eventId
            """)
    Mono<Integer> markPublished(String eventId, Instant publishedAt);

    @Query("""
            UPDATE outbox_events
            SET status = CASE WHEN retry_count + 1 >= :maxRetries THEN 'FAILED' ELSE 'PENDING' END,
                retry_count = retry_count + 1,
                last_error = :errorMessage,
                updated_at = :updatedAt
            WHERE event_id = :eventId
            """)
    Mono<Integer> markFailed(String eventId, String errorMessage, Instant updatedAt, int maxRetries);
}

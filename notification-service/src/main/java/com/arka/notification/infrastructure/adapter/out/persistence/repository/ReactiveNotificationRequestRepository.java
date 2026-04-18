package com.arka.notification.infrastructure.adapter.out.persistence.repository;

import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationRequestRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveNotificationRequestRepository extends ReactiveCrudRepository<NotificationRequestRow, String> {

    @Query("SELECT * FROM notification_requests WHERE organization_id = :organizationId AND notification_id = :notificationId")
    Mono<NotificationRequestRow> findByOrganizationAndId(String organizationId, String notificationId);

    @Query("SELECT * FROM notification_requests WHERE organization_id = :organizationId AND notification_key = :notificationKey")
    Mono<NotificationRequestRow> findByOrganizationAndKey(String organizationId, String notificationKey);

    @Query("""
            SELECT *
            FROM notification_requests
            WHERE status IN ('PENDING', 'FAILED')
              AND retryable = TRUE
              AND (next_retry_at IS NULL OR next_retry_at <= :at)
            ORDER BY COALESCE(next_retry_at, created_at), created_at
            LIMIT :limit
            """)
    Flux<NotificationRequestRow> findDispatchable(Instant at, int limit);

    @Query("""
            UPDATE notification_requests
            SET template_id = :templateId,
                channel_policy_id = :channelPolicyId,
                payload_json = :payloadJson,
                status = :status,
                retryable = :retryable,
                next_retry_at = :nextRetryAt,
                max_attempts = :maxAttempts,
                attempt_count = :attemptCount,
                trace_id = :traceId,
                correlation_id = :correlationId,
                version = :nextVersion,
                updated_at = :updatedAt
            WHERE organization_id = :organizationId
              AND notification_id = :notificationId
              AND version = :expectedVersion
            """)
    Mono<Integer> updateOptimistic(
            String organizationId,
            String notificationId,
            String templateId,
            String channelPolicyId,
            String payloadJson,
            String status,
            boolean retryable,
            Instant nextRetryAt,
            int maxAttempts,
            int attemptCount,
            String traceId,
            String correlationId,
            long expectedVersion,
            long nextVersion,
            Instant updatedAt);
}

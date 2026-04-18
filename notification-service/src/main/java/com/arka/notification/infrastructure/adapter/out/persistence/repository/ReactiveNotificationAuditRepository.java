package com.arka.notification.infrastructure.adapter.out.persistence.repository;

import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationAuditRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveNotificationAuditRepository extends ReactiveCrudRepository<NotificationAuditRow, String> {

    @Query("""
            SELECT *
            FROM notification_audits
            WHERE tenant_id = :tenantId
              AND action_type = :actionType
              AND idempotency_key = :idempotencyKey
            ORDER BY created_at DESC
            LIMIT 1
            """)
    Mono<NotificationAuditRow> findByIdempotency(String tenantId, String actionType, String idempotencyKey);

    @Query("""
            SELECT *
            FROM notification_audits
            WHERE tenant_id = :tenantId
              AND (:targetType IS NULL OR target_type = :targetType)
              AND (:targetId IS NULL OR target_id = :targetId)
            ORDER BY created_at DESC
            OFFSET :offset
            LIMIT :limit
            """)
    Flux<NotificationAuditRow> findByTarget(String tenantId, String targetType, String targetId, int offset, int limit);

    @Query("""
            SELECT COUNT(*)
            FROM notification_audits
            WHERE tenant_id = :tenantId
              AND (:targetType IS NULL OR target_type = :targetType)
              AND (:targetId IS NULL OR target_id = :targetId)
            """)
    Mono<Long> countByTarget(String tenantId, String targetType, String targetId);
}

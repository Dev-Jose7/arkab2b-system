package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ReportingAuditRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveReportingAuditRepository extends ReactiveCrudRepository<ReportingAuditRow, String> {

    @Query("""
            SELECT *
            FROM reporting_audits
            WHERE organization_id = :organizationId
              AND action_type = :actionType
              AND idempotency_key = :idempotencyKey
            ORDER BY created_at DESC
            LIMIT 1
            """)
    Mono<ReportingAuditRow> findByIdempotency(String organizationId, String actionType, String idempotencyKey);

    @Query("""
            SELECT *
            FROM reporting_audits
            WHERE organization_id = :organizationId
              AND (:targetType IS NULL OR target_type = :targetType)
              AND (:targetId IS NULL OR target_id = :targetId)
            ORDER BY created_at DESC
            OFFSET :offset
            LIMIT :limit
            """)
    Flux<ReportingAuditRow> findByTarget(String organizationId, String targetType, String targetId, int offset, int limit);

    @Query("""
            SELECT COUNT(*)
            FROM reporting_audits
            WHERE organization_id = :organizationId
              AND (:targetType IS NULL OR target_type = :targetType)
              AND (:targetId IS NULL OR target_id = :targetId)
            """)
    Mono<Long> countByTarget(String organizationId, String targetType, String targetId);
}

package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.IdempotencyRecordEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface IdempotencyRecordR2dbcRepository extends ReactiveCrudRepository<IdempotencyRecordEntity, String> {

    @Query("""
            SELECT *
            FROM idempotency_records
            WHERE organization_id = :organizationId
              AND operation_name = :operationName
              AND idempotency_key = :idempotencyKey
            """)
    Mono<IdempotencyRecordEntity> findByOrganizationOperationAndKey(String organizationId, String operationName, String idempotencyKey);
}

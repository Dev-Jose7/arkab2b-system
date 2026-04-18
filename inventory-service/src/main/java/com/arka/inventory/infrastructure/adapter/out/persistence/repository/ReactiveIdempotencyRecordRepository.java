package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.IdempotencyRecordRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveIdempotencyRecordRepository extends ReactiveCrudRepository<IdempotencyRecordRow, String> {

    @Query("SELECT idempotency_id, tenant_id, operation_name, idempotency_key, request_hash, resource_type, resource_id, response_status, created_at, updated_at FROM idempotency_records WHERE tenant_id = :tenantId AND operation_name = :operationName AND idempotency_key = :idempotencyKey")
    Mono<IdempotencyRecordRow> findByTenantOperationAndKey(String tenantId, String operationName, String idempotencyKey);
}

package com.arka.order.application.port.out.persistence;

import com.arka.order.domain.order.entity.IdempotencyRecord;
import reactor.core.publisher.Mono;

public interface IdempotencyRecordPersistencePort {

    Mono<IdempotencyRecord> findByOrganizationOperationAndKey(String organizationId, String operationName, String idempotencyKey);

    Mono<IdempotencyRecord> save(IdempotencyRecord idempotencyRecord);
}

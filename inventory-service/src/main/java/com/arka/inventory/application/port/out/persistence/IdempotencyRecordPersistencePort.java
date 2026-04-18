package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.IdempotencyRecord;
import reactor.core.publisher.Mono;

public interface IdempotencyRecordPersistencePort {

    Mono<IdempotencyRecord> findByOrganizationOperationAndKey(String organizationId, String operationName, String idempotencyKey);

    Mono<IdempotencyRecord> save(IdempotencyRecord idempotencyRecord);
}

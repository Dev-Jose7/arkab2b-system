package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.application.port.out.persistence.IdempotencyRecordPersistencePort;
import com.arka.order.domain.order.entity.IdempotencyRecord;
import com.arka.order.infrastructure.adapter.out.persistence.entity.IdempotencyRecordEntity;
import com.arka.order.infrastructure.adapter.out.persistence.repository.IdempotencyRecordR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IdempotencyRecordR2dbcAdapter implements IdempotencyRecordPersistencePort {

    private final IdempotencyRecordR2dbcRepository repository;
    private final R2dbcEntityTemplate entityTemplate;

    public IdempotencyRecordR2dbcAdapter(
            IdempotencyRecordR2dbcRepository repository,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<IdempotencyRecord> findByOrganizationOperationAndKey(String organizationId, String operationName, String idempotencyKey) {
        return repository.findByOrganizationOperationAndKey(organizationId, operationName, idempotencyKey)
                .map(this::toDomain);
    }

    @Override
    public Mono<IdempotencyRecord> save(IdempotencyRecord idempotencyRecord) {
        IdempotencyRecordEntity entity = toEntity(idempotencyRecord);
        return repository.existsById(entity.idempotencyId())
                .flatMap(exists -> exists ? repository.save(entity) : entityTemplate.insert(entity))
                .map(this::toDomain);
    }

    private IdempotencyRecordEntity toEntity(IdempotencyRecord record) {
        return new IdempotencyRecordEntity(
                record.idempotencyId(),
                record.organizationId(),
                record.operationName(),
                record.idempotencyKey(),
                record.requestHash(),
                record.resourceType(),
                record.resourceId(),
                record.responseStatus(),
                record.createdAt(),
                record.updatedAt());
    }

    private IdempotencyRecord toDomain(IdempotencyRecordEntity entity) {
        return new IdempotencyRecord(
                entity.idempotencyId(),
                entity.organizationId(),
                entity.operationName(),
                entity.idempotencyKey(),
                entity.requestHash(),
                entity.resourceType(),
                entity.resourceId(),
                entity.responseStatus(),
                entity.createdAt(),
                entity.updatedAt());
    }
}

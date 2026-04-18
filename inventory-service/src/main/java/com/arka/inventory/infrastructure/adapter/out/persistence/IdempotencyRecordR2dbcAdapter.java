package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.IdempotencyRecordPersistencePort;
import com.arka.inventory.domain.inventorybalance.entity.IdempotencyRecord;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveIdempotencyRecordRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IdempotencyRecordR2dbcAdapter implements IdempotencyRecordPersistencePort {

    private final ReactiveIdempotencyRecordRepository repository;
    private final InventoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public IdempotencyRecordR2dbcAdapter(
            ReactiveIdempotencyRecordRepository repository,
            InventoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<IdempotencyRecord> findByTenantOperationAndKey(
            String tenantId,
            String operationName,
            String idempotencyKey) {
        return repository.findByTenantOperationAndKey(tenantId, operationName, idempotencyKey)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<IdempotencyRecord> save(IdempotencyRecord idempotencyRecord) {
        var row = rowMapper.toRow(idempotencyRecord);
        return repository.existsById(row.idempotencyId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }
}

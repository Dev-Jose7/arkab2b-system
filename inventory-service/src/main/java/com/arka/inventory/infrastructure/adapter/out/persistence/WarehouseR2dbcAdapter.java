package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.WarehousePersistencePort;
import com.arka.inventory.domain.inventorybalance.entity.Warehouse;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveWarehouseRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class WarehouseR2dbcAdapter implements WarehousePersistencePort {

    private final ReactiveWarehouseRepository repository;
    private final InventoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public WarehouseR2dbcAdapter(
            ReactiveWarehouseRepository repository,
            InventoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Boolean> existsByTenantAndCode(String tenantId, String warehouseCode) {
        return repository.existsByTenantAndCode(tenantId, warehouseCode);
    }

    @Override
    public Mono<Warehouse> save(Warehouse warehouse) {
        var row = rowMapper.toRow(warehouse);
        return repository.existsById(row.warehouseId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Warehouse> findById(String tenantId, String warehouseId) {
        return repository.findByTenantAndId(tenantId, warehouseId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<Warehouse> findByTenant(String tenantId) {
        return repository.findByTenant(tenantId).map(rowMapper::toDomain);
    }
}

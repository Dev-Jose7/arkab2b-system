package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.StockMovementPersistencePort;
import com.arka.inventory.domain.inventorybalance.entity.StockMovement;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveStockMovementRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class StockMovementR2dbcAdapter implements StockMovementPersistencePort {

    private final ReactiveStockMovementRepository repository;
    private final InventoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public StockMovementR2dbcAdapter(
            ReactiveStockMovementRepository repository,
            InventoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<StockMovement> save(StockMovement movement) {
        return entityTemplate.insert(rowMapper.toRow(movement)).map(rowMapper::toDomain);
    }

    @Override
    public Flux<StockMovement> findByOrganizationAndStockItem(String organizationId, String stockItemId, int limit) {
        return repository.findByOrganizationAndStockItem(organizationId, stockItemId, limit).map(rowMapper::toDomain);
    }
}

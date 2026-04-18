package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.StockItemPersistencePort;
import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveStockItemRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class StockItemR2dbcAdapter implements StockItemPersistencePort {

    private final ReactiveStockItemRepository repository;
    private final InventoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public StockItemR2dbcAdapter(
            ReactiveStockItemRepository repository,
            InventoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Boolean> existsByOrganizationWarehouseSku(String organizationId, String warehouseId, String sku) {
        return repository.existsByOrganizationWarehouseSku(organizationId, warehouseId, sku);
    }

    @Override
    public Mono<StockItem> insert(StockItem stockItem) {
        return entityTemplate.insert(rowMapper.toRow(stockItem)).map(rowMapper::toDomain);
    }

    @Override
    public Mono<StockItem> findById(String organizationId, String stockItemId) {
        return repository.findByOrganizationAndId(organizationId, stockItemId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<StockItem> findByOrganizationWarehouseSku(String organizationId, String warehouseId, String sku) {
        return repository.findByOrganizationWarehouseSku(organizationId, warehouseId, sku).map(rowMapper::toDomain);
    }

    @Override
    public Flux<StockItem> findByOrganizationAndWarehouse(String organizationId, String warehouseId) {
        return repository.findByOrganizationAndWarehouse(organizationId, warehouseId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<StockItem> findLowStockByOrganizationAndWarehouse(String organizationId, String warehouseId) {
        return repository.findLowStockByOrganizationAndWarehouse(organizationId, warehouseId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Boolean> updateWithExpectedVersion(StockItem stockItem, long expectedVersion) {
        return repository.updateWithExpectedVersion(
                        stockItem.organizationId(),
                        stockItem.stockItemId(),
                        stockItem.physicalQty(),
                        stockItem.reservedQty(),
                        stockItem.reorderPoint(),
                        stockItem.safetyStock(),
                        stockItem.status().name(),
                        stockItem.version(),
                        stockItem.updatedAt(),
                        expectedVersion)
                .map(rows -> rows != null && rows == 1);
    }
}

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
    public Mono<Boolean> existsByTenantWarehouseSku(String tenantId, String warehouseId, String sku) {
        return repository.existsByTenantWarehouseSku(tenantId, warehouseId, sku);
    }

    @Override
    public Mono<StockItem> insert(StockItem stockItem) {
        return entityTemplate.insert(rowMapper.toRow(stockItem)).map(rowMapper::toDomain);
    }

    @Override
    public Mono<StockItem> findById(String tenantId, String stockItemId) {
        return repository.findByTenantAndId(tenantId, stockItemId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<StockItem> findByTenantWarehouseSku(String tenantId, String warehouseId, String sku) {
        return repository.findByTenantWarehouseSku(tenantId, warehouseId, sku).map(rowMapper::toDomain);
    }

    @Override
    public Flux<StockItem> findByTenantAndWarehouse(String tenantId, String warehouseId) {
        return repository.findByTenantAndWarehouse(tenantId, warehouseId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<StockItem> findLowStockByTenantAndWarehouse(String tenantId, String warehouseId) {
        return repository.findLowStockByTenantAndWarehouse(tenantId, warehouseId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Boolean> updateWithExpectedVersion(StockItem stockItem, long expectedVersion) {
        return repository.updateWithExpectedVersion(
                        stockItem.tenantId(),
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

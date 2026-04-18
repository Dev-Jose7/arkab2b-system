package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockItemPersistencePort {

    Mono<Boolean> existsByTenantWarehouseSku(String tenantId, String warehouseId, String sku);

    Mono<StockItem> insert(StockItem stockItem);

    Mono<StockItem> findById(String tenantId, String stockItemId);

    Mono<StockItem> findByTenantWarehouseSku(String tenantId, String warehouseId, String sku);

    Flux<StockItem> findByTenantAndWarehouse(String tenantId, String warehouseId);

    Flux<StockItem> findLowStockByTenantAndWarehouse(String tenantId, String warehouseId);

    Mono<Boolean> updateWithExpectedVersion(StockItem stockItem, long expectedVersion);
}

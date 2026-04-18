package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockItemPersistencePort {

    Mono<Boolean> existsByOrganizationWarehouseSku(String organizationId, String warehouseId, String sku);

    Mono<StockItem> insert(StockItem stockItem);

    Mono<StockItem> findById(String organizationId, String stockItemId);

    Mono<StockItem> findByOrganizationWarehouseSku(String organizationId, String warehouseId, String sku);

    Flux<StockItem> findByOrganizationAndWarehouse(String organizationId, String warehouseId);

    Flux<StockItem> findLowStockByOrganizationAndWarehouse(String organizationId, String warehouseId);

    Mono<Boolean> updateWithExpectedVersion(StockItem stockItem, long expectedVersion);
}

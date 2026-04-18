package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.StockMovement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockMovementPersistencePort {

    Mono<StockMovement> save(StockMovement movement);

    Flux<StockMovement> findByTenantAndStockItem(String tenantId, String stockItemId, int limit);
}

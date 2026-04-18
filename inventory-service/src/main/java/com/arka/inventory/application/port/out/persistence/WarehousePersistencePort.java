package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.Warehouse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarehousePersistencePort {

    Mono<Boolean> existsByTenantAndCode(String tenantId, String warehouseCode);

    Mono<Warehouse> save(Warehouse warehouse);

    Mono<Warehouse> findById(String tenantId, String warehouseId);

    Flux<Warehouse> findByTenant(String tenantId);
}

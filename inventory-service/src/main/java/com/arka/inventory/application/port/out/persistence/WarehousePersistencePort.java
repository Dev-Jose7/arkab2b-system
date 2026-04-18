package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.Warehouse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarehousePersistencePort {

    Mono<Boolean> existsByOrganizationAndCode(String organizationId, String warehouseCode);

    Mono<Warehouse> save(Warehouse warehouse);

    Mono<Warehouse> findById(String organizationId, String warehouseId);

    Flux<Warehouse> findByOrganization(String organizationId);
}

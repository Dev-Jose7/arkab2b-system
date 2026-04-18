package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.WarehouseRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveWarehouseRepository extends ReactiveCrudRepository<WarehouseRow, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM warehouses WHERE organization_id = :organizationId AND UPPER(warehouse_code) = UPPER(:warehouseCode))")
    Mono<Boolean> existsByOrganizationAndCode(String organizationId, String warehouseCode);

    @Query("SELECT warehouse_id, organization_id, warehouse_code, warehouse_name, country_code, status, created_at, updated_at FROM warehouses WHERE organization_id = :organizationId AND warehouse_id = :warehouseId")
    Mono<WarehouseRow> findByOrganizationAndId(String organizationId, String warehouseId);

    @Query("SELECT warehouse_id, organization_id, warehouse_code, warehouse_name, country_code, status, created_at, updated_at FROM warehouses WHERE organization_id = :organizationId ORDER BY warehouse_code")
    Flux<WarehouseRow> findByOrganization(String organizationId);
}

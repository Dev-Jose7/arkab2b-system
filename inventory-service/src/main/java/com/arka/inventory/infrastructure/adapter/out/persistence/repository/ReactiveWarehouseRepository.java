package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.WarehouseRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveWarehouseRepository extends ReactiveCrudRepository<WarehouseRow, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM warehouses WHERE tenant_id = :tenantId AND UPPER(warehouse_code) = UPPER(:warehouseCode))")
    Mono<Boolean> existsByTenantAndCode(String tenantId, String warehouseCode);

    @Query("SELECT warehouse_id, tenant_id, warehouse_code, warehouse_name, country_code, status, created_at, updated_at FROM warehouses WHERE tenant_id = :tenantId AND warehouse_id = :warehouseId")
    Mono<WarehouseRow> findByTenantAndId(String tenantId, String warehouseId);

    @Query("SELECT warehouse_id, tenant_id, warehouse_code, warehouse_name, country_code, status, created_at, updated_at FROM warehouses WHERE tenant_id = :tenantId ORDER BY warehouse_code")
    Flux<WarehouseRow> findByTenant(String tenantId);
}

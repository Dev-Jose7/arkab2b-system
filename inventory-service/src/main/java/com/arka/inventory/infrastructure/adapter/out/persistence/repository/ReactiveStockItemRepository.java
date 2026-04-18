package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.StockItemRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveStockItemRepository extends ReactiveCrudRepository<StockItemRow, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM stock_items WHERE tenant_id = :tenantId AND warehouse_id = :warehouseId AND UPPER(sku) = UPPER(:sku))")
    Mono<Boolean> existsByTenantWarehouseSku(String tenantId, String warehouseId, String sku);

    @Query("SELECT stock_item_id, tenant_id, warehouse_id, sku, physical_qty, reserved_qty, reorder_point, safety_stock, status, version, created_at, updated_at FROM stock_items WHERE tenant_id = :tenantId AND stock_item_id = :stockItemId")
    Mono<StockItemRow> findByTenantAndId(String tenantId, String stockItemId);

    @Query("SELECT stock_item_id, tenant_id, warehouse_id, sku, physical_qty, reserved_qty, reorder_point, safety_stock, status, version, created_at, updated_at FROM stock_items WHERE tenant_id = :tenantId AND warehouse_id = :warehouseId AND UPPER(sku) = UPPER(:sku)")
    Mono<StockItemRow> findByTenantWarehouseSku(String tenantId, String warehouseId, String sku);

    @Query("SELECT stock_item_id, tenant_id, warehouse_id, sku, physical_qty, reserved_qty, reorder_point, safety_stock, status, version, created_at, updated_at FROM stock_items WHERE tenant_id = :tenantId AND warehouse_id = :warehouseId ORDER BY sku")
    Flux<StockItemRow> findByTenantAndWarehouse(String tenantId, String warehouseId);

    @Query("SELECT stock_item_id, tenant_id, warehouse_id, sku, physical_qty, reserved_qty, reorder_point, safety_stock, status, version, created_at, updated_at FROM stock_items WHERE tenant_id = :tenantId AND warehouse_id = :warehouseId AND (physical_qty - reserved_qty) <= reorder_point ORDER BY (physical_qty - reserved_qty) ASC")
    Flux<StockItemRow> findLowStockByTenantAndWarehouse(String tenantId, String warehouseId);

    @Modifying
    @Query("""
            UPDATE stock_items
            SET physical_qty = :physicalQty,
                reserved_qty = :reservedQty,
                reorder_point = :reorderPoint,
                safety_stock = :safetyStock,
                status = :status,
                version = :newVersion,
                updated_at = :updatedAt
            WHERE tenant_id = :tenantId
              AND stock_item_id = :stockItemId
              AND version = :expectedVersion
            """)
    Mono<Integer> updateWithExpectedVersion(
            @Param("tenantId") String tenantId,
            @Param("stockItemId") String stockItemId,
            @Param("physicalQty") Integer physicalQty,
            @Param("reservedQty") Integer reservedQty,
            @Param("reorderPoint") Integer reorderPoint,
            @Param("safetyStock") Integer safetyStock,
            @Param("status") String status,
            @Param("newVersion") Long newVersion,
            @Param("updatedAt") Instant updatedAt,
            @Param("expectedVersion") Long expectedVersion);
}

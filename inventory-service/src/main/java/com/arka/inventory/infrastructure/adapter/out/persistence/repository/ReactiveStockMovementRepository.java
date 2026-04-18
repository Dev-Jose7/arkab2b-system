package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.StockMovementRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReactiveStockMovementRepository extends ReactiveCrudRepository<StockMovementRow, String> {

    @Query("""
            SELECT movement_id, tenant_id, stock_item_id, warehouse_id, sku, movement_type, delta_qty,
                   reason, reservation_id, order_id, correlation_id, created_at
            FROM stock_movements
            WHERE tenant_id = :tenantId
              AND stock_item_id = :stockItemId
            ORDER BY created_at DESC
            LIMIT :limit
            """)
    Flux<StockMovementRow> findByTenantAndStockItem(
            @Param("tenantId") String tenantId,
            @Param("stockItemId") String stockItemId,
            @Param("limit") int limit);
}

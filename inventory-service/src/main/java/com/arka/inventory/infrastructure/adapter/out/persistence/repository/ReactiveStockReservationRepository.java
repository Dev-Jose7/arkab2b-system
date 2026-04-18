package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.StockReservationRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveStockReservationRepository extends ReactiveCrudRepository<StockReservationRow, String> {

    @Query("SELECT reservation_id, organization_id, stock_item_id, warehouse_id, sku, cart_id, order_id, qty, status, expires_at, confirmed_at, released_at, created_at, updated_at FROM stock_reservations WHERE organization_id = :organizationId AND reservation_id = :reservationId")
    Mono<StockReservationRow> findByOrganizationAndId(String organizationId, String reservationId);

    @Query("SELECT reservation_id, organization_id, stock_item_id, warehouse_id, sku, cart_id, order_id, qty, status, expires_at, confirmed_at, released_at, created_at, updated_at FROM stock_reservations WHERE organization_id = :organizationId AND cart_id = :cartId ORDER BY created_at DESC")
    Flux<StockReservationRow> findByOrganizationAndCart(String organizationId, String cartId);

    @Query("""
            SELECT reservation_id, organization_id, stock_item_id, warehouse_id, sku, cart_id, order_id, qty, status,
                   expires_at, confirmed_at, released_at, created_at, updated_at
            FROM stock_reservations
            WHERE organization_id = :organizationId
              AND status = 'ACTIVE'
              AND expires_at IS NOT NULL
              AND expires_at <= :now
            ORDER BY expires_at ASC
            LIMIT :limit
            """)
    Flux<StockReservationRow> findExpiredActive(@Param("organizationId") String organizationId, @Param("now") Instant now, @Param("limit") int limit);
}

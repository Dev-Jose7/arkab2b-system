package com.arka.inventory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("stock_reservations")
public record StockReservationRow(
        @Id @Column("reservation_id") String reservationId,
        @Column("organization_id") String organizationId,
        @Column("stock_item_id") String stockItemId,
        @Column("warehouse_id") String warehouseId,
        @Column("sku") String sku,
        @Column("cart_id") String cartId,
        @Column("order_id") String orderId,
        @Column("qty") Integer qty,
        @Column("status") String status,
        @Column("expires_at") Instant expiresAt,
        @Column("confirmed_at") Instant confirmedAt,
        @Column("released_at") Instant releasedAt,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}

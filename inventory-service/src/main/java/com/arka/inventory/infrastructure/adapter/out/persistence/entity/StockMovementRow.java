package com.arka.inventory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("stock_movements")
public record StockMovementRow(
        @Id @Column("movement_id") String movementId,
        @Column("tenant_id") String tenantId,
        @Column("stock_item_id") String stockItemId,
        @Column("warehouse_id") String warehouseId,
        @Column("sku") String sku,
        @Column("movement_type") String movementType,
        @Column("delta_qty") Integer deltaQty,
        @Column("reason") String reason,
        @Column("reservation_id") String reservationId,
        @Column("order_id") String orderId,
        @Column("correlation_id") String correlationId,
        @Column("created_at") Instant createdAt) {}

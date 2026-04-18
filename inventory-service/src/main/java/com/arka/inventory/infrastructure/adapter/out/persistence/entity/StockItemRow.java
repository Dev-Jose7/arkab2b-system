package com.arka.inventory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("stock_items")
public record StockItemRow(
        @Id @Column("stock_item_id") String stockItemId,
        @Column("organization_id") String organizationId,
        @Column("warehouse_id") String warehouseId,
        @Column("sku") String sku,
        @Column("physical_qty") Integer physicalQty,
        @Column("reserved_qty") Integer reservedQty,
        @Column("reorder_point") Integer reorderPoint,
        @Column("safety_stock") Integer safetyStock,
        @Column("status") String status,
        @Column("version") Long version,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}

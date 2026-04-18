package com.arka.inventory.domain.inventorybalance.event;

import java.time.Instant;

public final class StockUpdated extends AbstractInventoryDomainEvent {

    private final String organizationId;
    private final String warehouseId;
    private final String sku;
    private final int physicalQty;
    private final int reservedQty;
    private final String reason;

    public StockUpdated(
            Instant occurredAt,
            String stockItemId,
            String organizationId,
            String warehouseId,
            String sku,
            int physicalQty,
            int reservedQty,
            String reason) {
        super("StockUpdated", occurredAt, stockItemId, "InventoryBalance");
        this.organizationId = organizationId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.physicalQty = physicalQty;
        this.reservedQty = reservedQty;
        this.reason = reason;
    }

    public String organizationId() {
        return organizationId;
    }

    public String warehouseId() {
        return warehouseId;
    }

    public String sku() {
        return sku;
    }

    public int physicalQty() {
        return physicalQty;
    }

    public int reservedQty() {
        return reservedQty;
    }

    public String reason() {
        return reason;
    }
}

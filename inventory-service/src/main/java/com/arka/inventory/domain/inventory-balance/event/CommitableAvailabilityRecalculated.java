package com.arka.inventory.domain.inventorybalance.event;

import java.time.Instant;

public final class CommitableAvailabilityRecalculated extends AbstractInventoryDomainEvent {

    private final String organizationId;
    private final String warehouseId;
    private final String sku;
    private final int availableQty;
    private final boolean lowStock;
    private final String reason;

    public CommitableAvailabilityRecalculated(
            Instant occurredAt,
            String stockItemId,
            String organizationId,
            String warehouseId,
            String sku,
            int availableQty,
            boolean lowStock,
            String reason) {
        super("CommitableAvailabilityRecalculated", occurredAt, stockItemId, "InventoryBalance");
        this.organizationId = organizationId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.availableQty = availableQty;
        this.lowStock = lowStock;
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

    public int availableQty() {
        return availableQty;
    }

    public boolean lowStock() {
        return lowStock;
    }

    public String reason() {
        return reason;
    }
}

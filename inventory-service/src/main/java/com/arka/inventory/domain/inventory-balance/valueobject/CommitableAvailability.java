package com.arka.inventory.domain.inventorybalance.valueobject;

public record CommitableAvailability(
        String tenantId,
        String warehouseId,
        String sku,
        int physicalQty,
        int reservedQty,
        int availableQty,
        int reorderPoint,
        int safetyStock,
        boolean lowStock) {

    public static CommitableAvailability from(
            String tenantId,
            String warehouseId,
            String sku,
            int physicalQty,
            int reservedQty,
            int reorderPoint,
            int safetyStock) {
        int available = physicalQty - reservedQty;
        boolean lowStock = available <= reorderPoint;
        return new CommitableAvailability(
                tenantId,
                warehouseId,
                sku,
                physicalQty,
                reservedQty,
                available,
                reorderPoint,
                safetyStock,
                lowStock);
    }
}

package com.arka.inventory.domain.inventorybalance.enumtype;

public enum StockItemStatus {
    ACTIVE,
    BLOCKED,
    RECONCILING;

    public boolean allowsReservations() {
        return this == ACTIVE;
    }

    public boolean allowsStockMutations() {
        return this != BLOCKED;
    }
}

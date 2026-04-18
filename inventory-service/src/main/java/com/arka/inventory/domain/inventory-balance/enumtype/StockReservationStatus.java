package com.arka.inventory.domain.inventorybalance.enumtype;

public enum StockReservationStatus {
    ACTIVE,
    CONFIRMED,
    RELEASED,
    EXPIRED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isClosed() {
        return this == CONFIRMED || this == RELEASED || this == EXPIRED;
    }
}

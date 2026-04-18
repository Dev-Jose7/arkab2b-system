package com.arka.inventory.domain.inventorybalance.exception;

public class ReservationNotActiveException extends InventoryDomainException {

    public ReservationNotActiveException(String message) {
        super("reservation_not_active", message);
    }
}

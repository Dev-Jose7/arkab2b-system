package com.arka.inventory.domain.inventorybalance.exception;

public class InvalidReservationTransitionException extends InventoryDomainException {

    public InvalidReservationTransitionException(String message) {
        super("invalid_reservation_transition", message);
    }
}

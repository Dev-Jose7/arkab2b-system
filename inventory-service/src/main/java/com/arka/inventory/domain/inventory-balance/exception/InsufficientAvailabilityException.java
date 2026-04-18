package com.arka.inventory.domain.inventorybalance.exception;

public class InsufficientAvailabilityException extends InventoryDomainException {

    public InsufficientAvailabilityException(String message) {
        super("insufficient_availability", message);
    }
}

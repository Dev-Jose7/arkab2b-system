package com.arka.inventory.application.exception;

public class InventoryValidationException extends ApplicationException {

    public InventoryValidationException(String message) {
        super("inventory_validation_error", message);
    }
}

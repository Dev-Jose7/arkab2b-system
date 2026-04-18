package com.arka.inventory.application.exception;

public class InventoryConflictException extends ApplicationException {

    public InventoryConflictException(String message) {
        super("inventory_conflict", message);
    }
}

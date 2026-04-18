package com.arka.inventory.application.exception;

public class InventoryResourceNotFoundException extends ApplicationException {

    public InventoryResourceNotFoundException(String message) {
        super("inventory_resource_not_found", message);
    }
}

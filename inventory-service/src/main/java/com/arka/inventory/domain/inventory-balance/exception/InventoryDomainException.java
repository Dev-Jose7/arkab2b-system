package com.arka.inventory.domain.inventorybalance.exception;

import com.arka.inventory.domain.shared.exception.DomainException;

public class InventoryDomainException extends DomainException {

    public InventoryDomainException(String code, String message) {
        super(code, message);
    }
}

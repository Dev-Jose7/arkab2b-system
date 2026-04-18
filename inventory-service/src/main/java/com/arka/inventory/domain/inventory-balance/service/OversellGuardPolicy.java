package com.arka.inventory.domain.inventorybalance.service;

import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.domain.inventorybalance.exception.InsufficientAvailabilityException;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import org.springframework.stereotype.Component;

@Component
public class OversellGuardPolicy {

    public void assertReservationAllowed(StockItem stockItem, int qty) {
        if (stockItem == null) {
            throw new DomainInvariantViolationException("stockItem is required");
        }
        if (qty <= 0) {
            throw new DomainInvariantViolationException("qty must be positive");
        }
        if (qty > stockItem.availableQty()) {
            throw new InsufficientAvailabilityException("Requested reservation qty exceeds available_qty");
        }
    }
}

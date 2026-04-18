package com.arka.inventory.domain.inventorybalance.aggregate;

import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.domain.inventorybalance.enumtype.StockItemStatus;
import com.arka.inventory.domain.inventorybalance.event.CommitableAvailabilityRecalculated;
import com.arka.inventory.domain.inventorybalance.event.StockUpdated;
import com.arka.inventory.domain.inventorybalance.valueobject.CommitableAvailability;
import com.arka.inventory.domain.shared.event.DomainEvent;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class InventoryBalance {

    private StockItem stockItem;
    private final List<DomainEvent> domainEvents;

    private InventoryBalance(StockItem stockItem, List<DomainEvent> domainEvents) {
        if (stockItem == null) {
            throw new DomainInvariantViolationException("stockItem is required");
        }
        this.stockItem = stockItem;
        this.domainEvents = domainEvents == null ? new ArrayList<>() : domainEvents;
    }

    public static InventoryBalance from(StockItem stockItem) {
        return new InventoryBalance(stockItem, new ArrayList<>());
    }

    public void updateOperationalStock(int deltaQty, String reason, Instant now) {
        this.stockItem = stockItem.adjustPhysical(deltaQty, now, reason);
        registerStockAndAvailabilityEvents(now, reason);
    }

    public StockReservation reserveStock(
            String reservationId,
            String cartId,
            int qty,
            Instant expiresAt,
            Instant now) {
        stockItem.ensureReservable(qty);
        this.stockItem = stockItem.reserve(qty, now);
        registerStockAndAvailabilityEvents(now, "ReserveStock");
        return StockReservation.createActive(
                reservationId,
                stockItem.organizationId(),
                stockItem.stockItemId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                cartId,
                qty,
                expiresAt,
                now);
    }

    public StockReservation confirmReservation(StockReservation activeReservation, String orderId, Instant now) {
        activeReservation.ensureActiveAndNotExpired(now);
        StockReservation confirmed = activeReservation.confirm(orderId, now);
        this.stockItem = stockItem.releaseReserved(activeReservation.qty(), now).adjustPhysical(-activeReservation.qty(), now, "ConfirmReservation");
        registerStockAndAvailabilityEvents(now, "ConfirmReservation");
        return confirmed;
    }

    public StockReservation releaseReservation(StockReservation activeReservation, Instant now) {
        activeReservation.ensureActiveAndNotExpired(now);
        StockReservation released = activeReservation.release(now);
        this.stockItem = stockItem.releaseReserved(activeReservation.qty(), now);
        registerStockAndAvailabilityEvents(now, "ReleaseReservation");
        return released;
    }

    public StockReservation expireReservation(StockReservation activeReservation, Instant now) {
        StockReservation expired = activeReservation.expire(now);
        this.stockItem = stockItem.releaseReserved(activeReservation.qty(), now);
        registerStockAndAvailabilityEvents(now, "ExpireReservation");
        return expired;
    }

    public void recalculateCommitableAvailability(String reason, Instant now) {
        appendAvailabilityEvent(now, reason == null || reason.isBlank() ? "RecalculateCommitableAvailability" : reason.trim());
    }

    public void markStockStatus(StockItemStatus status, Instant now, String reason) {
        this.stockItem = stockItem.markStatus(status, now);
        registerStockAndAvailabilityEvents(now, reason == null ? "MarkStockStatus" : reason);
    }

    public CommitableAvailability committableAvailability() {
        return CommitableAvailability.from(
                stockItem.organizationId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                stockItem.physicalQty(),
                stockItem.reservedQty(),
                stockItem.reorderPoint(),
                stockItem.safetyStock());
    }

    public StockItem stockItem() {
        return stockItem;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private void registerStockAndAvailabilityEvents(Instant now, String reason) {
        Instant occurredAt = now == null ? Instant.now() : now;
        String normalizedReason = reason == null ? "InventoryMutation" : reason.trim();
        domainEvents.add(new StockUpdated(
                occurredAt,
                stockItem.stockItemId(),
                stockItem.organizationId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                stockItem.physicalQty(),
                stockItem.reservedQty(),
                normalizedReason));
        appendAvailabilityEvent(occurredAt, normalizedReason);
    }

    private void appendAvailabilityEvent(Instant now, String reason) {
        CommitableAvailability availability = committableAvailability();
        domainEvents.add(new CommitableAvailabilityRecalculated(
                now,
                stockItem.stockItemId(),
                stockItem.organizationId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                availability.availableQty(),
                availability.lowStock(),
                reason));
    }
}

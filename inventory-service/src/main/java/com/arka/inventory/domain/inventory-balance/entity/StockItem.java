package com.arka.inventory.domain.inventorybalance.entity;

import com.arka.inventory.domain.inventorybalance.enumtype.StockItemStatus;
import com.arka.inventory.domain.inventorybalance.exception.InsufficientAvailabilityException;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public final class StockItem {

    private final String stockItemId;
    private final String organizationId;
    private final String warehouseId;
    private final String sku;
    private final int physicalQty;
    private final int reservedQty;
    private final int reorderPoint;
    private final int safetyStock;
    private final StockItemStatus status;
    private final long version;
    private final Instant createdAt;
    private final Instant updatedAt;

    public StockItem(
            String stockItemId,
            String organizationId,
            String warehouseId,
            String sku,
            int physicalQty,
            int reservedQty,
            int reorderPoint,
            int safetyStock,
            StockItemStatus status,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        this.stockItemId = requireNotBlank(stockItemId, "stockItemId");
        this.organizationId = requireNotBlank(organizationId, "organizationId");
        this.warehouseId = requireNotBlank(warehouseId, "warehouseId");
        this.sku = requireNotBlank(sku, "sku").toUpperCase();
        this.physicalQty = physicalQty;
        this.reservedQty = reservedQty;
        this.reorderPoint = reorderPoint;
        this.safetyStock = safetyStock;
        this.status = status == null ? StockItemStatus.ACTIVE : status;
        this.version = version;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        validateInvariants();
    }

    public static StockItem initialize(
            String stockItemId,
            String organizationId,
            String warehouseId,
            String sku,
            int initialPhysicalQty,
            int reorderPoint,
            int safetyStock,
            Instant now) {
        Instant created = now == null ? Instant.now() : now;
        return new StockItem(
                stockItemId,
                organizationId,
                warehouseId,
                sku,
                initialPhysicalQty,
                0,
                reorderPoint,
                safetyStock,
                StockItemStatus.ACTIVE,
                0,
                created,
                created);
    }

    public StockItem adjustPhysical(int deltaQty, Instant now, String reason) {
        requireReason(reason);
        if (!status.allowsStockMutations()) {
            throw new DomainInvariantViolationException("Stock item state does not allow stock mutation");
        }
        int nextPhysical = physicalQty + deltaQty;
        if (nextPhysical < 0) {
            throw new DomainInvariantViolationException("physical_qty cannot be negative");
        }
        if (reservedQty > nextPhysical) {
            throw new DomainInvariantViolationException("reserved_qty cannot exceed physical_qty");
        }
        return copy(nextPhysical, reservedQty, status, now);
    }

    public StockItem reserve(int qty, Instant now) {
        ensureReservable(qty);
        int nextReserved = reservedQty + qty;
        if (nextReserved > physicalQty) {
            throw new InsufficientAvailabilityException("Reservation exceeds physical stock");
        }
        return copy(physicalQty, nextReserved, status, now);
    }

    public StockItem releaseReserved(int qty, Instant now) {
        if (qty <= 0) {
            throw new DomainInvariantViolationException("qty to release must be positive");
        }
        if (qty > reservedQty) {
            throw new DomainInvariantViolationException("Cannot release more than currently reserved");
        }
        return copy(physicalQty, reservedQty - qty, status, now);
    }

    public StockItem markStatus(StockItemStatus targetStatus, Instant now) {
        if (targetStatus == null) {
            throw new DomainInvariantViolationException("stock item status is required");
        }
        return copy(physicalQty, reservedQty, targetStatus, now);
    }

    public int availableQty() {
        return physicalQty - reservedQty;
    }

    public boolean lowStock() {
        return availableQty() <= reorderPoint;
    }

    public void ensureReservable(int qty) {
        if (qty <= 0) {
            throw new DomainInvariantViolationException("Reservation qty must be positive");
        }
        if (!status.allowsReservations()) {
            throw new DomainInvariantViolationException("Stock item status does not allow reservations");
        }
        if (qty > availableQty()) {
            throw new InsufficientAvailabilityException("Reservation qty exceeds available_qty");
        }
    }

    public StockItem withIncrementedVersion(Instant now) {
        return new StockItem(
                stockItemId,
                organizationId,
                warehouseId,
                sku,
                physicalQty,
                reservedQty,
                reorderPoint,
                safetyStock,
                status,
                version + 1,
                createdAt,
                now == null ? Instant.now() : now);
    }

    private StockItem copy(int nextPhysicalQty, int nextReservedQty, StockItemStatus nextStatus, Instant now) {
        return new StockItem(
                stockItemId,
                organizationId,
                warehouseId,
                sku,
                nextPhysicalQty,
                nextReservedQty,
                reorderPoint,
                safetyStock,
                nextStatus,
                version,
                createdAt,
                now == null ? Instant.now() : now);
    }

    private void validateInvariants() {
        if (physicalQty < 0) {
            throw new DomainInvariantViolationException("physical_qty cannot be negative");
        }
        if (reservedQty < 0) {
            throw new DomainInvariantViolationException("reserved_qty cannot be negative");
        }
        if (reservedQty > physicalQty) {
            throw new DomainInvariantViolationException("reserved_qty cannot exceed physical_qty");
        }
        if (reorderPoint < 0 || safetyStock < 0) {
            throw new DomainInvariantViolationException("reorder point and safety stock must be non-negative");
        }
        if (version < 0) {
            throw new DomainInvariantViolationException("version must be non-negative");
        }
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
        return value.trim();
    }

    private static void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new DomainInvariantViolationException("reason is required");
        }
    }

    public String stockItemId() {
        return stockItemId;
    }

    public String organizationId() {
        return organizationId;
    }

    public String warehouseId() {
        return warehouseId;
    }

    public String sku() {
        return sku;
    }

    public int physicalQty() {
        return physicalQty;
    }

    public int reservedQty() {
        return reservedQty;
    }

    public int reorderPoint() {
        return reorderPoint;
    }

    public int safetyStock() {
        return safetyStock;
    }

    public StockItemStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

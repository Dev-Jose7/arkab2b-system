package com.arka.inventory.domain.inventorybalance.service;

import com.arka.inventory.domain.inventorybalance.aggregate.InventoryBalance;
import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.domain.inventorybalance.enumtype.StockReservationStatus;
import com.arka.inventory.domain.inventorybalance.exception.InsufficientAvailabilityException;
import com.arka.inventory.domain.inventorybalance.exception.InvalidReservationTransitionException;
import com.arka.inventory.domain.inventorybalance.exception.ReservationNotActiveException;
import com.arka.inventory.domain.shared.event.DomainEvent;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryBalanceDomainModelTest {

    @Test
    void reserveStockFailsWhenQtyExceedsAvailable() {
        StockItem stockItem = new StockItem(
                "stock-1",
                "tenant-1",
                "wh-1",
                "SKU-1",
                5,
                4,
                2,
                1,
                null,
                0,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
        InventoryBalance balance = InventoryBalance.from(stockItem);

        assertThrows(
                InsufficientAvailabilityException.class,
                () -> balance.reserveStock(
                        "res-1",
                        "cart-1",
                        2,
                        Instant.parse("2026-01-01T01:00:00Z"),
                        Instant.parse("2026-01-01T00:30:00Z")));
    }

    @Test
    void confirmReservationConsumesPhysicalAndReservedQuantities() {
        StockItem stockItem = new StockItem(
                "stock-1",
                "tenant-1",
                "wh-1",
                "SKU-1",
                10,
                3,
                2,
                1,
                null,
                0,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));

        StockReservation reservation = new StockReservation(
                "res-1",
                "tenant-1",
                "stock-1",
                "wh-1",
                "SKU-1",
                "cart-1",
                null,
                3,
                StockReservationStatus.ACTIVE,
                Instant.parse("2026-01-01T05:00:00Z"),
                null,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));

        InventoryBalance balance = InventoryBalance.from(stockItem);
        StockReservation confirmed = balance.confirmReservation(
                reservation,
                "order-1",
                Instant.parse("2026-01-01T01:00:00Z"));

        assertEquals(StockReservationStatus.CONFIRMED, confirmed.status());
        assertEquals(7, balance.stockItem().physicalQty());
        assertEquals(0, balance.stockItem().reservedQty());

        List<DomainEvent> events = balance.pullDomainEvents();
        assertEquals(2, events.size());
        assertEquals("StockUpdated", events.get(0).eventType());
        assertEquals("CommitableAvailabilityRecalculated", events.get(1).eventType());
    }

    @Test
    void stockItemInvariantRejectsReservedAbovePhysical() {
        assertThrows(
                DomainInvariantViolationException.class,
                () -> new StockItem(
                        "stock-1",
                        "tenant-1",
                        "wh-1",
                        "SKU-1",
                        1,
                        2,
                        0,
                        0,
                        null,
                        0,
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-01T00:00:00Z")));
    }

    @Test
    void confirmedReservationCannotExpire() {
        StockReservation confirmed = new StockReservation(
                "res-1",
                "tenant-1",
                "stock-1",
                "wh-1",
                "SKU-1",
                "cart-1",
                "order-1",
                1,
                StockReservationStatus.CONFIRMED,
                Instant.parse("2026-01-01T01:00:00Z"),
                Instant.parse("2026-01-01T00:30:00Z"),
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:30:00Z"));

        assertThrows(
                InvalidReservationTransitionException.class,
                () -> confirmed.expire(Instant.parse("2026-01-01T02:00:00Z")));
    }

    @Test
    void reservationExpiringExactlyAtNowCannotBeConfirmed() {
        StockReservation expiringNow = new StockReservation(
                "res-2",
                "tenant-1",
                "stock-1",
                "wh-1",
                "SKU-1",
                "cart-1",
                null,
                1,
                StockReservationStatus.ACTIVE,
                Instant.parse("2026-01-01T01:00:00Z"),
                null,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));

        assertThrows(
                ReservationNotActiveException.class,
                () -> expiringNow.ensureActiveAndNotExpired(Instant.parse("2026-01-01T01:00:00Z")));
    }
}

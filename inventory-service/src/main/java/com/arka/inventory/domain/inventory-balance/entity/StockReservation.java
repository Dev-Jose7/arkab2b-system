package com.arka.inventory.domain.inventorybalance.entity;

import com.arka.inventory.domain.inventorybalance.enumtype.StockReservationStatus;
import com.arka.inventory.domain.inventorybalance.exception.InvalidReservationTransitionException;
import com.arka.inventory.domain.inventorybalance.exception.ReservationNotActiveException;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public final class StockReservation {

    private final String reservationId;
    private final String tenantId;
    private final String stockItemId;
    private final String warehouseId;
    private final String sku;
    private final String cartId;
    private final String orderId;
    private final int qty;
    private final StockReservationStatus status;
    private final Instant expiresAt;
    private final Instant confirmedAt;
    private final Instant releasedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    public StockReservation(
            String reservationId,
            String tenantId,
            String stockItemId,
            String warehouseId,
            String sku,
            String cartId,
            String orderId,
            int qty,
            StockReservationStatus status,
            Instant expiresAt,
            Instant confirmedAt,
            Instant releasedAt,
            Instant createdAt,
            Instant updatedAt) {
        this.reservationId = requireNotBlank(reservationId, "reservationId");
        this.tenantId = requireNotBlank(tenantId, "tenantId");
        this.stockItemId = requireNotBlank(stockItemId, "stockItemId");
        this.warehouseId = requireNotBlank(warehouseId, "warehouseId");
        this.sku = requireNotBlank(sku, "sku").toUpperCase();
        this.cartId = normalizeOptional(cartId);
        this.orderId = normalizeOptional(orderId);
        this.qty = qty;
        this.status = status == null ? StockReservationStatus.ACTIVE : status;
        this.expiresAt = expiresAt;
        this.confirmedAt = confirmedAt;
        this.releasedAt = releasedAt;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        validateInvariants();
    }

    public static StockReservation createActive(
            String reservationId,
            String tenantId,
            String stockItemId,
            String warehouseId,
            String sku,
            String cartId,
            int qty,
            Instant expiresAt,
            Instant now) {
        return new StockReservation(
                reservationId,
                tenantId,
                stockItemId,
                warehouseId,
                sku,
                cartId,
                null,
                qty,
                StockReservationStatus.ACTIVE,
                expiresAt,
                null,
                null,
                now,
                now);
    }

    public StockReservation confirm(String orderId, Instant now) {
        ensureActiveAndNotExpired(now);
        if (orderId == null || orderId.isBlank()) {
            throw new DomainInvariantViolationException("orderId is required to confirm reservation");
        }
        return new StockReservation(
                reservationId,
                tenantId,
                stockItemId,
                warehouseId,
                sku,
                cartId,
                orderId.trim(),
                qty,
                StockReservationStatus.CONFIRMED,
                expiresAt,
                now,
                releasedAt,
                createdAt,
                now);
    }

    public StockReservation release(Instant now) {
        ensureActiveOrConfirmed();
        return new StockReservation(
                reservationId,
                tenantId,
                stockItemId,
                warehouseId,
                sku,
                cartId,
                orderId,
                qty,
                StockReservationStatus.RELEASED,
                expiresAt,
                confirmedAt,
                now,
                createdAt,
                now);
    }

    public StockReservation expire(Instant now) {
        if (status != StockReservationStatus.ACTIVE) {
            throw new InvalidReservationTransitionException("Only ACTIVE reservation can expire");
        }
        if (expiresAt != null && expiresAt.isAfter(now)) {
            throw new InvalidReservationTransitionException("Reservation cannot expire before expires_at");
        }
        return new StockReservation(
                reservationId,
                tenantId,
                stockItemId,
                warehouseId,
                sku,
                cartId,
                orderId,
                qty,
                StockReservationStatus.EXPIRED,
                expiresAt,
                confirmedAt,
                now,
                createdAt,
                now);
    }

    public void ensureActiveAndNotExpired(Instant now) {
        if (status != StockReservationStatus.ACTIVE) {
            throw new ReservationNotActiveException("Reservation is not active");
        }
        if (isExpired(now)) {
            throw new ReservationNotActiveException("Reservation is expired");
        }
    }

    public boolean isExpired(Instant now) {
        if (expiresAt == null) {
            return false;
        }
        Instant reference = now == null ? Instant.now() : now;
        return !expiresAt.isAfter(reference);
    }

    private void ensureActiveOrConfirmed() {
        if (status != StockReservationStatus.ACTIVE && status != StockReservationStatus.CONFIRMED) {
            throw new InvalidReservationTransitionException("Reservation cannot be released from current status");
        }
    }

    private void validateInvariants() {
        if (qty <= 0) {
            throw new DomainInvariantViolationException("Reservation qty must be positive");
        }
        if (status == StockReservationStatus.ACTIVE && expiresAt == null) {
            throw new DomainInvariantViolationException("Active reservation requires expiresAt");
        }
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public String reservationId() {
        return reservationId;
    }

    public String tenantId() {
        return tenantId;
    }

    public String stockItemId() {
        return stockItemId;
    }

    public String warehouseId() {
        return warehouseId;
    }

    public String sku() {
        return sku;
    }

    public String cartId() {
        return cartId;
    }

    public String orderId() {
        return orderId;
    }

    public int qty() {
        return qty;
    }

    public StockReservationStatus status() {
        return status;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant confirmedAt() {
        return confirmedAt;
    }

    public Instant releasedAt() {
        return releasedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

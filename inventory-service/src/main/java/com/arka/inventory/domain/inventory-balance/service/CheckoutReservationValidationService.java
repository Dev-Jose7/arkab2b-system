package com.arka.inventory.domain.inventorybalance.service;

import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.domain.inventorybalance.exception.ReservationNotActiveException;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CheckoutReservationValidationService {

    public void ensureConfirmable(StockReservation reservation, Instant now) {
        if (reservation == null) {
            throw new DomainInvariantViolationException("reservation is required");
        }
        reservation.ensureActiveAndNotExpired(now == null ? Instant.now() : now);
        if (reservation.qty() <= 0) {
            throw new ReservationNotActiveException("Reservation qty is invalid");
        }
    }
}

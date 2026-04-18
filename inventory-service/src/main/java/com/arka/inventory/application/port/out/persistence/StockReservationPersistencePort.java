package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockReservationPersistencePort {

    Mono<StockReservation> save(StockReservation reservation);

    Mono<StockReservation> findById(String organizationId, String reservationId);

    Flux<StockReservation> findByOrganizationAndCart(String organizationId, String cartId);

    Flux<StockReservation> findExpiredActive(String organizationId, Instant now, int limit);
}

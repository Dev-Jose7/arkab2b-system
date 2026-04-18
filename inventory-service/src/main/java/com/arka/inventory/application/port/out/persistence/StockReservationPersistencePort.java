package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockReservationPersistencePort {

    Mono<StockReservation> save(StockReservation reservation);

    Mono<StockReservation> findById(String tenantId, String reservationId);

    Flux<StockReservation> findByTenantAndCart(String tenantId, String cartId);

    Flux<StockReservation> findExpiredActive(String tenantId, Instant now, int limit);
}

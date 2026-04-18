package com.arka.inventory.application.port.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.ReservationLedger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReservationLedgerPersistencePort {

    Mono<ReservationLedger> save(ReservationLedger ledger);

    Flux<ReservationLedger> findByOrganizationAndReservation(String organizationId, String reservationId);
}

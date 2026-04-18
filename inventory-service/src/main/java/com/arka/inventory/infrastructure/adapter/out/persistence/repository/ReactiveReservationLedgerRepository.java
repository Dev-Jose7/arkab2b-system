package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.ReservationLedgerRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReactiveReservationLedgerRepository extends ReactiveCrudRepository<ReservationLedgerRow, String> {

    @Query("SELECT ledger_id, tenant_id, reservation_id, entry_type, qty, note, created_at FROM reservation_ledgers WHERE tenant_id = :tenantId AND reservation_id = :reservationId ORDER BY created_at DESC")
    Flux<ReservationLedgerRow> findByTenantAndReservation(String tenantId, String reservationId);
}

package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.ReservationLedgerPersistencePort;
import com.arka.inventory.domain.inventorybalance.entity.ReservationLedger;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveReservationLedgerRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReservationLedgerR2dbcAdapter implements ReservationLedgerPersistencePort {

    private final ReactiveReservationLedgerRepository repository;
    private final InventoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public ReservationLedgerR2dbcAdapter(
            ReactiveReservationLedgerRepository repository,
            InventoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<ReservationLedger> save(ReservationLedger ledger) {
        return entityTemplate.insert(rowMapper.toRow(ledger)).map(rowMapper::toDomain);
    }

    @Override
    public Flux<ReservationLedger> findByTenantAndReservation(String tenantId, String reservationId) {
        return repository.findByTenantAndReservation(tenantId, reservationId).map(rowMapper::toDomain);
    }
}

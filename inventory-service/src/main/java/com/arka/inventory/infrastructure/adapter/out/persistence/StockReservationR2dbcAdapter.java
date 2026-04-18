package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.StockReservationPersistencePort;
import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveStockReservationRepository;
import java.time.Instant;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class StockReservationR2dbcAdapter implements StockReservationPersistencePort {

    private final ReactiveStockReservationRepository repository;
    private final InventoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public StockReservationR2dbcAdapter(
            ReactiveStockReservationRepository repository,
            InventoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<StockReservation> save(StockReservation reservation) {
        var row = rowMapper.toRow(reservation);
        return repository.existsById(row.reservationId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<StockReservation> findById(String tenantId, String reservationId) {
        return repository.findByTenantAndId(tenantId, reservationId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<StockReservation> findByTenantAndCart(String tenantId, String cartId) {
        return repository.findByTenantAndCart(tenantId, cartId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<StockReservation> findExpiredActive(String tenantId, Instant now, int limit) {
        return repository.findExpiredActive(tenantId, now, limit).map(rowMapper::toDomain);
    }
}

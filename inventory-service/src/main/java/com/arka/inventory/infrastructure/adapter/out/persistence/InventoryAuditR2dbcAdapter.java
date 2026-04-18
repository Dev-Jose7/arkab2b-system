package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.audit.InventoryAuditPort;
import com.arka.inventory.application.result.InventoryAuditEntryResult;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveInventoryAuditRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class InventoryAuditR2dbcAdapter implements InventoryAuditPort {

    private final ReactiveInventoryAuditRepository repository;

    public InventoryAuditR2dbcAdapter(ReactiveInventoryAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> record(
            String tenantId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String outcome,
            String payload) {
        Instant now = Instant.now();
        return repository.insert(
                        UUID.randomUUID().toString(),
                        tenantId,
                        actorUserId,
                        actionType,
                        targetType,
                        targetId,
                        outcome,
                        payload == null ? "{}" : payload,
                        now)
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new IllegalStateException("Inventory audit insert did not affect exactly one row")));
    }

    @Override
    public Flux<InventoryAuditEntryResult> findByTenant(String tenantId, int limit) {
        return repository.findByTenant(tenantId, limit)
                .map(row -> new InventoryAuditEntryResult(
                        row.auditId(),
                        row.tenantId(),
                        row.actorUserId(),
                        row.actionType(),
                        row.targetType(),
                        row.targetId(),
                        row.outcome(),
                        row.payload(),
                        row.createdAt()));
    }
}

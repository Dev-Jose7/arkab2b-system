package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.audit.DirectoryAuditPort;
import com.arka.directory.application.result.DirectoryAuditEntryResult;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveDirectoryAuditRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DirectoryAuditR2dbcAdapter implements DirectoryAuditPort {

    private final ReactiveDirectoryAuditRepository repository;

    public DirectoryAuditR2dbcAdapter(ReactiveDirectoryAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> record(
            String organizationId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String outcome,
            String payload) {
        return repository.insert(
                        UUID.randomUUID().toString(),
                        organizationId,
                        actorUserId,
                        actionType,
                        targetType,
                        targetId,
                        outcome,
                        payload == null || payload.isBlank() ? "{}" : payload,
                        Instant.now())
                .then();
    }

    @Override
    public Flux<DirectoryAuditEntryResult> findByOrganization(String organizationId, int limit) {
        return repository.findByOrganization(organizationId, limit).map(row -> new DirectoryAuditEntryResult(
                row.auditId(),
                row.organizationId(),
                row.actorUserId(),
                row.actionType(),
                row.targetType(),
                row.targetId(),
                row.outcome(),
                row.payload(),
                row.createdAt()));
    }

    @Override
    public Mono<Long> countByOrganization(String organizationId) {
        return repository.countByOrganization(organizationId);
    }
}

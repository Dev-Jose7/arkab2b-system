package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.OutboxPersistencePort;
import com.arka.inventory.domain.shared.event.DomainEvent;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OutboxPersistenceAdapter implements OutboxPersistencePort {

    private final ReactiveOutboxEventRepository repository;
    private final OutboxRowMapper outboxRowMapper;

    public OutboxPersistenceAdapter(
            ReactiveOutboxEventRepository repository,
            OutboxRowMapper outboxRowMapper) {
        this.repository = repository;
        this.outboxRowMapper = outboxRowMapper;
    }

    @Override
    public Mono<Void> store(DomainEvent event) {
        OutboxEventRow row = outboxRowMapper.toRow(event);
        return repository.insert(
                        row.eventId(),
                        row.aggregateType(),
                        row.aggregateId(),
                        row.eventType(),
                        row.payload(),
                        row.status(),
                        row.occurredAt(),
                        row.publishedAt(),
                        row.retryCount(),
                        row.lastError(),
                        row.createdAt(),
                        row.updatedAt())
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new IllegalStateException("Outbox insert did not affect exactly one row")));
    }
}

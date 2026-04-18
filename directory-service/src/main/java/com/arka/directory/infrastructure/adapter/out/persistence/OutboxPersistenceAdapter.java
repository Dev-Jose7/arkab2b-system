package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.OutboxPersistencePort;
import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OutboxPersistenceAdapter implements OutboxPersistencePort {

    private final ReactiveOutboxEventRepository reactiveOutboxEventRepository;
    private final OutboxRowMapper outboxRowMapper;

    public OutboxPersistenceAdapter(
            ReactiveOutboxEventRepository reactiveOutboxEventRepository,
            OutboxRowMapper outboxRowMapper) {
        this.reactiveOutboxEventRepository = reactiveOutboxEventRepository;
        this.outboxRowMapper = outboxRowMapper;
    }

    @Override
    public Mono<Void> store(DomainEvent event) {
        OutboxEventRow row = outboxRowMapper.toRow(event);
        return reactiveOutboxEventRepository
                .insert(
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

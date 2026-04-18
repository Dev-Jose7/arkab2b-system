package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.application.port.out.persistence.OutboxPersistencePort;
import com.arka.order.application.port.out.persistence.OutboxRelayPort;
import com.arka.order.application.port.out.persistence.PendingOutboxEvent;
import com.arka.order.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.order.domain.shared.event.DomainEvent;
import com.arka.order.infrastructure.adapter.out.persistence.entity.ProcessedEventEntity;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.OutboxPersistenceMapper;
import com.arka.order.infrastructure.adapter.out.persistence.repository.OutboxEventR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.ProcessedEventR2dbcRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OutboxPersistenceR2dbcAdapter implements OutboxPersistencePort, OutboxRelayPort, ProcessedEventPersistencePort {

    private final OutboxEventR2dbcRepository outboxRepository;
    private final ProcessedEventR2dbcRepository processedEventRepository;
    private final OutboxPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    public OutboxPersistenceR2dbcAdapter(
            OutboxEventR2dbcRepository outboxRepository,
            ProcessedEventR2dbcRepository processedEventRepository,
            OutboxPersistenceMapper mapper,
            R2dbcEntityTemplate entityTemplate) {
        this.outboxRepository = outboxRepository;
        this.processedEventRepository = processedEventRepository;
        this.mapper = mapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Void> store(DomainEvent event) {
        return entityTemplate.insert(mapper.toEntity(event)).then();
    }

    @Override
    public Flux<PendingOutboxEvent> findPending(int batchSize) {
        return outboxRepository.findPending(batchSize).map(mapper::toPending);
    }

    @Override
    public Mono<Void> markPublished(String eventId, Instant publishedAt) {
        return outboxRepository.markPublished(eventId, publishedAt).then();
    }

    @Override
    public Mono<Void> markFailed(String eventId, String errorMessage, Instant failedAt, int maxRetries) {
        return outboxRepository.markFailed(eventId, errorMessage, failedAt, maxRetries).then();
    }

    @Override
    public Mono<Boolean> existsByEventAndConsumer(String eventId, String consumerName) {
        return processedEventRepository.existsByEventAndConsumer(eventId, consumerName)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> registerProcessed(String eventId, String consumerName) {
        ProcessedEventEntity entity = new ProcessedEventEntity(
                UUID.randomUUID().toString(),
                eventId,
                consumerName,
                Instant.now());
        return entityTemplate.insert(entity).then();
    }
}

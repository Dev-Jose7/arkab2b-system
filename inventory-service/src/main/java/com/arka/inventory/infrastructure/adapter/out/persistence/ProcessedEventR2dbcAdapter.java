package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.ProcessedEventRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveProcessedEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ProcessedEventR2dbcAdapter implements ProcessedEventPersistencePort {

    private final ReactiveProcessedEventRepository repository;
    private final R2dbcEntityTemplate entityTemplate;

    public ProcessedEventR2dbcAdapter(
            ReactiveProcessedEventRepository repository,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Boolean> existsByEventAndConsumer(String eventId, String consumerName) {
        return repository.existsByEventAndConsumer(eventId, consumerName);
    }

    @Override
    public Mono<Void> registerProcessed(String eventId, String consumerName) {
        ProcessedEventRow row = new ProcessedEventRow(
                UUID.randomUUID().toString(),
                eventId,
                consumerName,
                Instant.now());
        return entityTemplate.insert(row).then();
    }
}

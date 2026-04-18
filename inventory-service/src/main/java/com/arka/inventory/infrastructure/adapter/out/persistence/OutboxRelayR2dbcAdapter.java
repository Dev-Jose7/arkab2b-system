package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.port.out.persistence.OutboxRelayPort;
import com.arka.inventory.application.port.out.persistence.PendingOutboxEvent;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OutboxRelayR2dbcAdapter implements OutboxRelayPort {

    private final ReactiveOutboxEventRepository repository;

    public OutboxRelayR2dbcAdapter(ReactiveOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<PendingOutboxEvent> findPending(int batchSize) {
        return repository.findPending(batchSize)
                .map(row -> new PendingOutboxEvent(
                        row.eventId(),
                        row.aggregateType(),
                        row.aggregateId(),
                        row.eventType(),
                        row.payload(),
                        row.retryCount() == null ? 0 : row.retryCount()));
    }

    @Override
    public Mono<Void> markPublished(String eventId, Instant publishedAt) {
        return repository.markPublished(eventId, publishedAt)
                .flatMap(rowsUpdated -> rowsUpdated != null && rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new IllegalStateException("Outbox markPublished did not affect exactly one row")));
    }

    @Override
    public Mono<Void> markFailed(String eventId, String errorMessage, Instant failedAt, int maxRetries) {
        return repository.markFailed(eventId, errorMessage, failedAt, Math.max(1, maxRetries))
                .flatMap(rowsUpdated -> rowsUpdated != null && rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new IllegalStateException("Outbox markFailed did not affect exactly one row")));
    }
}

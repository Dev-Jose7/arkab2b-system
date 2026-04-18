package com.arka.reporting.application.port.out.persistence;

import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxRelayPort {

    Flux<PendingOutboxEvent> findPending(int limit);

    Mono<Void> markPublished(String eventId, Instant publishedAt);

    Mono<Void> markFailed(String eventId, String errorMessage, Instant updatedAt, int maxRetries);
}

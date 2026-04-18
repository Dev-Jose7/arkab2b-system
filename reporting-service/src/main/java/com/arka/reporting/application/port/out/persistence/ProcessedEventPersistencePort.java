package com.arka.reporting.application.port.out.persistence;

import java.time.Instant;
import reactor.core.publisher.Mono;

public interface ProcessedEventPersistencePort {

    Mono<Boolean> exists(String eventId, String consumerName);

    Mono<Void> record(String eventId, String consumerName, Instant processedAt);
}

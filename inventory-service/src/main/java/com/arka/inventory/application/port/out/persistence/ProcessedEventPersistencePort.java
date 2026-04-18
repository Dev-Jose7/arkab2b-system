package com.arka.inventory.application.port.out.persistence;

import reactor.core.publisher.Mono;

public interface ProcessedEventPersistencePort {

    Mono<Boolean> existsByEventAndConsumer(String eventId, String consumerName);

    Mono<Void> registerProcessed(String eventId, String consumerName);
}

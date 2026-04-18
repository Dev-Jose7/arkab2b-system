package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.shared.event.DomainEvent;
import reactor.core.publisher.Mono;

public interface OutboxPersistencePort {

    Mono<Void> store(DomainEvent event, String payload);
}

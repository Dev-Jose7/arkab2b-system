package com.arka.reporting.application.port.out.persistence;

import com.arka.reporting.domain.shared.event.DomainEvent;
import reactor.core.publisher.Mono;

public interface OutboxPersistencePort {

    Mono<Void> store(DomainEvent event, String payload);
}

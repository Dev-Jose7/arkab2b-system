package com.arka.notification.application.port.out.persistence;

import com.arka.notification.domain.shared.event.DomainEvent;
import reactor.core.publisher.Mono;

public interface OutboxPersistencePort {

    Mono<Void> store(DomainEvent event, String payload);
}

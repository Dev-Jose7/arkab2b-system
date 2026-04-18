package com.arka.notification.application.port.out.persistence;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationReadPersistencePort {

    Flux<NotificationSearchProjection> search(NotificationSearchFilter filter);

    Mono<Long> count(NotificationSearchFilter filter);

    Mono<NotificationMetricsProjection> metrics(String tenantId);
}

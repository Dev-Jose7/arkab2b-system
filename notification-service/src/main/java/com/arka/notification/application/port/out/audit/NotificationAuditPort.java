package com.arka.notification.application.port.out.audit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationAuditPort {

    Mono<Void> record(NotificationAuditEntry entry);

    Mono<NotificationAuditEntry> findByIdempotency(String organizationId, String actionType, String idempotencyKey);

    Flux<NotificationAuditEntry> findByTarget(String organizationId, String targetType, String targetId, int offset, int limit);

    Mono<Long> countByTarget(String organizationId, String targetType, String targetId);
}

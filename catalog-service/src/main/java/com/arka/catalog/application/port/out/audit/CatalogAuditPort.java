package com.arka.catalog.application.port.out.audit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogAuditPort {

    Mono<Void> record(CatalogAuditEntry entry);

    Mono<CatalogAuditEntry> findByIdempotency(String tenantId, String actionType, String idempotencyKey);

    Flux<CatalogAuditEntry> findByTarget(String tenantId, String targetType, String targetId, int offset, int limit);

    Mono<Long> countByTarget(String tenantId, String targetType, String targetId);
}

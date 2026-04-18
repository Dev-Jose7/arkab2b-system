package com.arka.catalog.application.port.out.audit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogAuditPort {

    Mono<Void> record(CatalogAuditEntry entry);

    Mono<CatalogAuditEntry> findByIdempotency(String organizationId, String actionType, String idempotencyKey);

    Flux<CatalogAuditEntry> findByTarget(String organizationId, String targetType, String targetId, int offset, int limit);

    Mono<Long> countByTarget(String organizationId, String targetType, String targetId);
}

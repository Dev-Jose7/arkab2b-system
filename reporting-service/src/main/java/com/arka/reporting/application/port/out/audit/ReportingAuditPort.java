package com.arka.reporting.application.port.out.audit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReportingAuditPort {

    Mono<Void> record(ReportingAuditEntry entry);

    Mono<ReportingAuditEntry> findByIdempotency(String organizationId, String actionType, String idempotencyKey);

    Flux<ReportingAuditEntry> findByTarget(String organizationId, String targetType, String targetId, int offset, int limit);

    Mono<Long> countByTarget(String organizationId, String targetType, String targetId);
}

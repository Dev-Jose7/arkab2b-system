package com.arka.reporting.application.port.out.audit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReportingAuditPort {

    Mono<Void> record(ReportingAuditEntry entry);

    Mono<ReportingAuditEntry> findByIdempotency(String tenantId, String actionType, String idempotencyKey);

    Flux<ReportingAuditEntry> findByTarget(String tenantId, String targetType, String targetId, int offset, int limit);

    Mono<Long> countByTarget(String tenantId, String targetType, String targetId);
}

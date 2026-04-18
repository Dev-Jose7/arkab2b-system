package com.arka.reporting.application.port.out.persistence;

import reactor.core.publisher.Mono;

public interface ReportingReadPersistencePort {

    Mono<ReportingMetricsProjection> metrics(String tenantId, String period);
}

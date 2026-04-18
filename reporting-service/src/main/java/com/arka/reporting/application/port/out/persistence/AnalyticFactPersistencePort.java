package com.arka.reporting.application.port.out.persistence;

import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.valueobject.FactId;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.analyticfact.valueobject.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AnalyticFactPersistencePort {

    Mono<AnalyticFact> create(AnalyticFact fact);

    Mono<AnalyticFact> update(AnalyticFact fact);

    Mono<AnalyticFact> findById(TenantId tenantId, FactId factId);

    Mono<AnalyticFact> findBySourceEventId(TenantId tenantId, SourceEventId sourceEventId);

    Flux<FactSearchProjection> search(FactSearchFilter filter);

    Mono<Long> count(FactSearchFilter filter);

    Flux<AnalyticFact> findAppliedByTenant(String tenantId);
}

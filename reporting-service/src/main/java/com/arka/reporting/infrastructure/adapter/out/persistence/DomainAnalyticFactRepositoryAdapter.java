package com.arka.reporting.infrastructure.adapter.out.persistence;

import com.arka.reporting.application.port.out.persistence.AnalyticFactPersistencePort;
import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.repository.AnalyticFactRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DomainAnalyticFactRepositoryAdapter implements AnalyticFactRepository {

    private final AnalyticFactPersistencePort persistencePort;

    public DomainAnalyticFactRepositoryAdapter(AnalyticFactPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Mono<AnalyticFact> save(AnalyticFact fact) {
        return persistencePort
                .findById(fact.organizationId(), fact.factId())
                .flatMap(existing -> persistencePort.update(fact))
                .switchIfEmpty(persistencePort.create(fact));
    }
}

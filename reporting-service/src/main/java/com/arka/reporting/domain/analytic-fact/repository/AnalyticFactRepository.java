package com.arka.reporting.domain.analyticfact.repository;

import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import reactor.core.publisher.Mono;

public interface AnalyticFactRepository {

    Mono<AnalyticFact> save(AnalyticFact fact);
}

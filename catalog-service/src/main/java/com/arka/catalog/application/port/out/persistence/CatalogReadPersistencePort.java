package com.arka.catalog.application.port.out.persistence;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogReadPersistencePort {

    Flux<CatalogSearchProjection> search(CatalogSearchFilter filter);

    Mono<Long> count(CatalogSearchFilter filter);
}

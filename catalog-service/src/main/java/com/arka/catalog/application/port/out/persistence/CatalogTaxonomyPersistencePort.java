package com.arka.catalog.application.port.out.persistence;

import reactor.core.publisher.Mono;

public interface CatalogTaxonomyPersistencePort {

    Mono<Boolean> isBrandActive(String organizationId, String brandId);

    Mono<Boolean> isCategoryActive(String organizationId, String categoryId);
}

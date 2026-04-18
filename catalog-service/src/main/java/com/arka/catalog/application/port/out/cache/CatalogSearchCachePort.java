package com.arka.catalog.application.port.out.cache;

import com.arka.catalog.application.result.CatalogSearchResult;
import reactor.core.publisher.Mono;

public interface CatalogSearchCachePort {

    Mono<CatalogSearchResult> get(String cacheKey);

    Mono<Void> put(String cacheKey, CatalogSearchResult result);

    Mono<Void> evictOrganization(String organizationId);
}

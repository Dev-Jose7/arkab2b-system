package com.arka.reporting.application.port.out.cache;

import com.arka.reporting.application.result.FactSearchResult;
import reactor.core.publisher.Mono;

public interface ReportingSearchCachePort {

    Mono<FactSearchResult> get(String cacheKey);

    Mono<Void> put(String cacheKey, FactSearchResult result);

    Mono<Void> evictTenant(String tenantId);
}

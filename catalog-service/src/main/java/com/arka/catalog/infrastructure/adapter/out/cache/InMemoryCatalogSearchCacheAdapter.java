package com.arka.catalog.infrastructure.adapter.out.cache;

import com.arka.catalog.application.port.out.cache.CatalogSearchCachePort;
import com.arka.catalog.application.result.CatalogSearchResult;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryCatalogSearchCacheAdapter implements CatalogSearchCachePort {

    private final ConcurrentHashMap<String, CatalogSearchResult> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<CatalogSearchResult> get(String cacheKey) {
        return Mono.justOrEmpty(cache.get(cacheKey));
    }

    @Override
    public Mono<Void> put(String cacheKey, CatalogSearchResult result) {
        cache.put(cacheKey, result);
        return Mono.empty();
    }

    @Override
    public Mono<Void> evictTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.empty();
        }
        String prefix = tenantId + "::";
        cache.keySet().removeIf(key -> key.startsWith(prefix));
        return Mono.empty();
    }
}

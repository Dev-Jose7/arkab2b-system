package com.arka.reporting.infrastructure.adapter.out.cache;

import com.arka.reporting.application.port.out.cache.ReportingSearchCachePort;
import com.arka.reporting.application.result.FactSearchResult;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryReportingSearchCacheAdapter implements ReportingSearchCachePort {

    private final ConcurrentHashMap<String, FactSearchResult> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<FactSearchResult> get(String cacheKey) {
        return Mono.justOrEmpty(cache.get(cacheKey));
    }

    @Override
    public Mono<Void> put(String cacheKey, FactSearchResult result) {
        cache.put(cacheKey, result);
        return Mono.empty();
    }

    @Override
    public Mono<Void> evictOrganization(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return Mono.empty();
        }
        String prefix = organizationId + "::";
        cache.keySet().removeIf(key -> key.startsWith(prefix));
        return Mono.empty();
    }
}

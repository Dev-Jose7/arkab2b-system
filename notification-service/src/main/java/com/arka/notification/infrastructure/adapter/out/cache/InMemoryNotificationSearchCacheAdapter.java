package com.arka.notification.infrastructure.adapter.out.cache;

import com.arka.notification.application.port.out.cache.NotificationSearchCachePort;
import com.arka.notification.application.result.NotificationSearchResult;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryNotificationSearchCacheAdapter implements NotificationSearchCachePort {

    private final ConcurrentHashMap<String, NotificationSearchResult> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<NotificationSearchResult> get(String cacheKey) {
        return Mono.justOrEmpty(cache.get(cacheKey));
    }

    @Override
    public Mono<Void> put(String cacheKey, NotificationSearchResult result) {
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

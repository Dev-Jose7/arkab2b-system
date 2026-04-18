package com.arka.inventory.infrastructure.adapter.out.cache;

import com.arka.inventory.application.port.out.cache.CommitableAvailabilityCachePort;
import com.arka.inventory.application.result.CommitableAvailabilityResult;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryCommitableAvailabilityCacheAdapter implements CommitableAvailabilityCachePort {

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;

    public InMemoryCommitableAvailabilityCacheAdapter(
            @Value("${app.redis.cache.commitable-availability-ttl-seconds:300}") long ttlSeconds) {
        this.ttl = Duration.ofSeconds(Math.max(30L, ttlSeconds));
    }

    @Override
    public Mono<CommitableAvailabilityResult> find(String organizationId, String warehouseId, String sku) {
        return Mono.defer(() -> {
            String key = cacheKey(organizationId, warehouseId, sku);
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                return Mono.empty();
            }
            if (entry.expiresAt().isBefore(Instant.now())) {
                cache.remove(key);
                return Mono.empty();
            }
            return Mono.just(entry.value());
        });
    }

    @Override
    public Mono<Void> put(CommitableAvailabilityResult availabilityResult) {
        if (availabilityResult == null) {
            return Mono.error(new IllegalArgumentException("availabilityResult is required"));
        }
        return Mono.fromRunnable(() -> cache.put(
                cacheKey(
                        availabilityResult.organizationId(),
                        availabilityResult.warehouseId(),
                        availabilityResult.sku()),
                new CacheEntry(availabilityResult, Instant.now().plus(ttl))));
    }

    @Override
    public Mono<Void> evict(String organizationId, String warehouseId, String sku) {
        return Mono.fromRunnable(() -> cache.remove(cacheKey(organizationId, warehouseId, sku)));
    }

    private String cacheKey(String organizationId, String warehouseId, String sku) {
        return "inventory:availability:" + organizationId + ":" + warehouseId + ":" + sku.toUpperCase();
    }

    private record CacheEntry(CommitableAvailabilityResult value, Instant expiresAt) {}
}

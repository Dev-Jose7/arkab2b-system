package com.arka.directory.infrastructure.adapter.out.cache;

import com.arka.directory.application.port.out.cache.DirectoryPolicyCachePort;
import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryDirectoryPolicyCacheAdapter implements DirectoryPolicyCachePort {

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;

    public InMemoryDirectoryPolicyCacheAdapter(
            @Value("${app.redis.cache.directory-policy-ttl-seconds:900}") long ttlSeconds) {
        this.ttl = Duration.ofSeconds(Math.max(60L, ttlSeconds));
    }

    @Override
    public Mono<CountryPolicy> findActive(String organizationId, String countryCode) {
        return Mono.defer(() -> {
            String key = cacheKey(organizationId, countryCode);
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                return Mono.empty();
            }
            if (entry.expiresAt().isBefore(Instant.now())) {
                cache.remove(key);
                return Mono.empty();
            }
            return Mono.just(entry.countryPolicy());
        });
    }

    @Override
    public Mono<Void> putActive(CountryPolicy countryPolicy) {
        if (countryPolicy == null) {
            return Mono.error(new IllegalArgumentException("countryPolicy is required"));
        }
        return Mono.fromRunnable(() -> cache.put(
                cacheKey(countryPolicy.organizationId().value(), countryPolicy.countryCode().value()),
                new CacheEntry(countryPolicy, Instant.now().plus(ttl))));
    }

    @Override
    public Mono<Void> evictActive(String organizationId, String countryCode) {
        return Mono.fromRunnable(() -> cache.remove(cacheKey(organizationId, countryCode)));
    }

    private String cacheKey(String organizationId, String countryCode) {
        return "directory:policy:" + organizationId + ":" + countryCode.toUpperCase();
    }

    private record CacheEntry(CountryPolicy countryPolicy, Instant expiresAt) {}
}

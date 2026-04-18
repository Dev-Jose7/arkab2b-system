package com.arka.order.infrastructure.adapter.out.cache;

import com.arka.order.application.port.out.cache.CheckoutAttemptCachePort;
import com.arka.order.application.result.CheckoutAttemptResult;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryCheckoutAttemptCacheAdapter implements CheckoutAttemptCachePort {

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;

    public InMemoryCheckoutAttemptCacheAdapter(
            @Value("${app.redis.cache.checkout-attempt-ttl-seconds:300}") long ttlSeconds) {
        this.ttl = Duration.ofSeconds(Math.max(30L, ttlSeconds));
    }

    @Override
    public Mono<CheckoutAttemptResult> findByCorrelation(String tenantId, String checkoutCorrelationId) {
        return Mono.defer(() -> {
            String key = cacheKey(tenantId, checkoutCorrelationId);
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
    public Mono<Void> put(CheckoutAttemptResult result) {
        if (result == null) {
            return Mono.error(new IllegalArgumentException("result is required"));
        }
        return Mono.fromRunnable(() -> cache.put(
                cacheKey(result.tenantId(), result.checkoutCorrelationId()),
                new CacheEntry(result, Instant.now().plus(ttl))));
    }

    @Override
    public Mono<Void> evict(String tenantId, String checkoutCorrelationId) {
        return Mono.fromRunnable(() -> cache.remove(cacheKey(tenantId, checkoutCorrelationId)));
    }

    private String cacheKey(String tenantId, String checkoutCorrelationId) {
        return "order:checkout-attempt:" + tenantId + ":" + checkoutCorrelationId;
    }

    private record CacheEntry(CheckoutAttemptResult value, Instant expiresAt) {}
}

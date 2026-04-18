package com.arka.order.infrastructure.adapter.out.cache;

import com.arka.order.application.port.out.cache.CheckoutAttemptCachePort;
import com.arka.order.application.result.CheckoutAttemptResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Primary
@ConditionalOnBean(ReactiveStringRedisTemplate.class)
public class RedisCheckoutAttemptCacheAdapter implements CheckoutAttemptCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisCheckoutAttemptCacheAdapter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;
    private final Counter readFailureCounter;
    private final Counter writeFailureCounter;
    private final Counter deserializeFailureCounter;

    public RedisCheckoutAttemptCacheAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            ObjectProvider<MeterRegistry> meterRegistryProvider,
            @Value("${app.redis.cache.checkout-attempt-ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(Math.max(30L, ttlSeconds));
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        this.readFailureCounter = counter(meterRegistry, "findByCorrelation");
        this.writeFailureCounter = counter(meterRegistry, "put");
        this.deserializeFailureCounter = counter(meterRegistry, "deserialize");
    }

    @Override
    public Mono<CheckoutAttemptResult> findByCorrelation(String tenantId, String checkoutCorrelationId) {
        // Cache is best-effort optimization; degrade to miss but keep failures observable.
        return redisTemplate.opsForValue()
                .get(cacheKey(tenantId, checkoutCorrelationId))
                .flatMap(this::fromJson)
                .onErrorResume(error -> {
                    log.warn(
                            "Redis cache read failed; degrading to cache miss. cache=checkout-attempt tenantId={} checkoutCorrelationId={}",
                            tenantId,
                            checkoutCorrelationId,
                            error);
                    increment(readFailureCounter);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> put(CheckoutAttemptResult result) {
        return Mono.fromCallable(() -> toJson(result))
                .flatMap(payload -> redisTemplate.opsForValue()
                        .set(cacheKey(result.tenantId(), result.checkoutCorrelationId()), payload, ttl)
                        .then())
                .onErrorResume(error -> {
                    log.warn(
                            "Redis cache write failed; continuing without cache. cache=checkout-attempt tenantId={} checkoutCorrelationId={}",
                            result.tenantId(),
                            result.checkoutCorrelationId(),
                            error);
                    increment(writeFailureCounter);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> evict(String tenantId, String checkoutCorrelationId) {
        return redisTemplate.delete(cacheKey(tenantId, checkoutCorrelationId)).then();
    }

    private Mono<CheckoutAttemptResult> fromJson(String payload) {
        try {
            return Mono.just(objectMapper.readValue(payload, CheckoutAttemptResult.class));
        } catch (JsonProcessingException exception) {
            log.warn("Redis cache payload deserialization failed; degrading to cache miss. cache=checkout-attempt", exception);
            increment(deserializeFailureCounter);
            return Mono.empty();
        }
    }

    private String toJson(CheckoutAttemptResult value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize checkout attempt cache", exception);
        }
    }

    private String cacheKey(String tenantId, String checkoutCorrelationId) {
        return "order:checkout-attempt:" + tenantId + ":" + checkoutCorrelationId;
    }

    private Counter counter(MeterRegistry meterRegistry, String operation) {
        if (meterRegistry == null) {
            return null;
        }
        return Counter.builder("order.cache.redis.failures")
                .description("Total Redis cache fallback events")
                .tag("cache", "checkout-attempt")
                .tag("operation", operation)
                .register(meterRegistry);
    }

    private void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }
}

package com.arka.inventory.infrastructure.adapter.out.cache;

import com.arka.inventory.application.port.out.cache.CommitableAvailabilityCachePort;
import com.arka.inventory.application.result.CommitableAvailabilityResult;
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
public class RedisCommitableAvailabilityCacheAdapter implements CommitableAvailabilityCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisCommitableAvailabilityCacheAdapter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;
    private final Counter readFailureCounter;
    private final Counter writeFailureCounter;
    private final Counter deserializeFailureCounter;

    public RedisCommitableAvailabilityCacheAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            ObjectProvider<MeterRegistry> meterRegistryProvider,
            @Value("${app.redis.cache.commitable-availability-ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(Math.max(30L, ttlSeconds));
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        this.readFailureCounter = counter(meterRegistry, "find");
        this.writeFailureCounter = counter(meterRegistry, "put");
        this.deserializeFailureCounter = counter(meterRegistry, "deserialize");
    }

    @Override
    public Mono<CommitableAvailabilityResult> find(String organizationId, String warehouseId, String sku) {
        // Cache is best-effort optimization; degrade to miss but keep failures observable.
        return redisTemplate.opsForValue()
                .get(cacheKey(organizationId, warehouseId, sku))
                .flatMap(this::fromJson)
                .onErrorResume(error -> {
                    log.warn(
                            "Redis cache read failed; degrading to cache miss. cache=commitable-availability organizationId={} warehouseId={} sku={}",
                            organizationId,
                            warehouseId,
                            sku,
                            error);
                    increment(readFailureCounter);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> put(CommitableAvailabilityResult availabilityResult) {
        return Mono.fromCallable(() -> toJson(availabilityResult))
                .flatMap(payload -> redisTemplate.opsForValue()
                        .set(
                                cacheKey(
                                        availabilityResult.organizationId(),
                                        availabilityResult.warehouseId(),
                                        availabilityResult.sku()),
                                payload,
                                ttl)
                        .then())
                .onErrorResume(error -> {
                    log.warn(
                            "Redis cache write failed; continuing without cache. cache=commitable-availability organizationId={} warehouseId={} sku={}",
                            availabilityResult.organizationId(),
                            availabilityResult.warehouseId(),
                            availabilityResult.sku(),
                            error);
                    increment(writeFailureCounter);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> evict(String organizationId, String warehouseId, String sku) {
        return redisTemplate.delete(cacheKey(organizationId, warehouseId, sku)).then();
    }

    private Mono<CommitableAvailabilityResult> fromJson(String payload) {
        try {
            return Mono.just(objectMapper.readValue(payload, CommitableAvailabilityResult.class));
        } catch (JsonProcessingException exception) {
            log.warn("Redis cache payload deserialization failed; degrading to cache miss. cache=commitable-availability", exception);
            increment(deserializeFailureCounter);
            return Mono.empty();
        }
    }

    private String toJson(CommitableAvailabilityResult availabilityResult) {
        try {
            return objectMapper.writeValueAsString(availabilityResult);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize cached availability", exception);
        }
    }

    private String cacheKey(String organizationId, String warehouseId, String sku) {
        return "inventory:availability:" + organizationId + ":" + warehouseId + ":" + sku.toUpperCase();
    }

    private Counter counter(MeterRegistry meterRegistry, String operation) {
        if (meterRegistry == null) {
            return null;
        }
        return Counter.builder("inventory.cache.redis.failures")
                .description("Total Redis cache fallback events")
                .tag("cache", "commitable-availability")
                .tag("operation", operation)
                .register(meterRegistry);
    }

    private void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }
}

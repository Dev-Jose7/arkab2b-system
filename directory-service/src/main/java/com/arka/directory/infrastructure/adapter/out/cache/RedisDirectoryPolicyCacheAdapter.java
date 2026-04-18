package com.arka.directory.infrastructure.adapter.out.cache;

import com.arka.directory.application.port.out.cache.DirectoryPolicyCachePort;
import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.domain.countrypolicy.enumtype.WeekStartsOn;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Primary
@ConditionalOnBean(ReactiveStringRedisTemplate.class)
public class RedisDirectoryPolicyCacheAdapter implements DirectoryPolicyCachePort {

    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Logger log = LoggerFactory.getLogger(RedisDirectoryPolicyCacheAdapter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Counter readFailureCounter;
    private final Counter writeFailureCounter;
    private final Counter deserializeFailureCounter;

    public RedisDirectoryPolicyCacheAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        this.readFailureCounter = counter(meterRegistry, "findActive");
        this.writeFailureCounter = counter(meterRegistry, "putActive");
        this.deserializeFailureCounter = counter(meterRegistry, "deserialize");
    }

    @Override
    public Mono<CountryPolicy> findActive(String organizationId, String countryCode) {
        // Cache is best-effort optimization; degrade to miss but keep failures observable.
        return redisTemplate.opsForValue()
                .get(cacheKey(organizationId, countryCode))
                .flatMap(this::fromJson)
                .onErrorResume(error -> {
                    log.warn(
                            "Redis cache read failed; degrading to cache miss. cache=directory-policy organizationId={} countryCode={}",
                            organizationId,
                            countryCode,
                            error);
                    increment(readFailureCounter);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> putActive(CountryPolicy countryPolicy) {
        return Mono.fromCallable(() -> toJson(CachedPolicy.fromDomain(countryPolicy)))
                .flatMap(json -> redisTemplate
                        .opsForValue()
                        .set(cacheKey(countryPolicy.organizationId().value(), countryPolicy.countryCode().value()), json, TTL)
                        .then())
                .onErrorResume(error -> {
                    log.warn(
                            "Redis cache write failed; continuing without cache. cache=directory-policy organizationId={} countryCode={} policyId={}",
                            countryPolicy.organizationId().value(),
                            countryPolicy.countryCode().value(),
                            countryPolicy.policyId(),
                            error);
                    increment(writeFailureCounter);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> evictActive(String organizationId, String countryCode) {
        return redisTemplate.delete(cacheKey(organizationId, countryCode)).then();
    }

    private Mono<CountryPolicy> fromJson(String json) {
        try {
            CachedPolicy cachedPolicy = objectMapper.readValue(json, CachedPolicy.class);
            return Mono.just(cachedPolicy.toDomain());
        } catch (JsonProcessingException exception) {
            log.warn("Redis cache payload deserialization failed; degrading to cache miss. cache=directory-policy", exception);
            increment(deserializeFailureCounter);
            return Mono.empty();
        }
    }

    private String toJson(CachedPolicy cachedPolicy) {
        try {
            return objectMapper.writeValueAsString(cachedPolicy);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize cached policy", exception);
        }
    }

    private String cacheKey(String organizationId, String countryCode) {
        return "directory:policy:" + organizationId + ":" + countryCode.toUpperCase();
    }

    private Counter counter(MeterRegistry meterRegistry, String operation) {
        if (meterRegistry == null) {
            return null;
        }
        return Counter.builder("directory.cache.redis.failures")
                .description("Total Redis cache fallback events")
                .tag("cache", "directory-policy")
                .tag("operation", operation)
                .register(meterRegistry);
    }

    private void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }

    record CachedPolicy(
            String policyId,
            String organizationId,
            String countryCode,
            long policyVersion,
            String currencyCode,
            String weekStartsOn,
            String weeklyCutoffLocalTime,
            String timezone,
            int reportingRetentionDays,
            boolean requiresVerifiedAddress,
            java.time.Instant effectiveFrom,
            java.time.Instant effectiveTo,
            String status,
            java.time.Instant createdAt,
            java.time.Instant updatedAt) {

        static CachedPolicy fromDomain(CountryPolicy countryPolicy) {
            return new CachedPolicy(
                    countryPolicy.policyId(),
                    countryPolicy.organizationId().value(),
                    countryPolicy.countryCode().value(),
                    countryPolicy.policyVersion().value(),
                    countryPolicy.currencyCode(),
                    countryPolicy.weekStartsOn().name(),
                    countryPolicy.weeklyCutoffLocalTime(),
                    countryPolicy.timezone(),
                    countryPolicy.reportingRetentionDays(),
                    countryPolicy.requiresVerifiedAddress(),
                    countryPolicy.effectiveFrom(),
                    countryPolicy.effectiveTo(),
                    countryPolicy.status().name(),
                    countryPolicy.createdAt(),
                    countryPolicy.updatedAt());
        }

        CountryPolicy toDomain() {
            return CountryPolicy.rehydrate(
                    policyId,
                    OrganizationId.of(organizationId),
                    CountryCode.of(countryCode),
                    PolicyVersion.of(policyVersion),
                    currencyCode,
                    WeekStartsOn.valueOf(weekStartsOn),
                    weeklyCutoffLocalTime,
                    timezone,
                    reportingRetentionDays,
                    requiresVerifiedAddress,
                    effectiveFrom,
                    effectiveTo,
                    CountryPolicyStatus.valueOf(status),
                    createdAt,
                    updatedAt);
        }
    }
}

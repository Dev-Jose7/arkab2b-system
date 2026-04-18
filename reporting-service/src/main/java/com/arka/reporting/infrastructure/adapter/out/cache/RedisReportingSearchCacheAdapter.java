package com.arka.reporting.infrastructure.adapter.out.cache;

import com.arka.reporting.application.port.out.cache.ReportingSearchCachePort;
import com.arka.reporting.application.result.FactSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Primary
@ConditionalOnBean({ReactiveStringRedisTemplate.class, RedisConnectionFactory.class})
@ConditionalOnProperty(prefix = "app.dependencies.redis", name = "enabled", havingValue = "true")
public class RedisReportingSearchCacheAdapter implements ReportingSearchCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisReportingSearchCacheAdapter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisReportingSearchCacheAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.redis.cache.fact-search-ttl-seconds:30}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(Math.max(1L, ttlSeconds));
    }

    @Override
    public Mono<FactSearchResult> get(String cacheKey) {
        return redisTemplate
                .opsForValue()
                .get(cacheKey)
                .flatMap(payload -> Mono.fromCallable(() -> objectMapper.readValue(payload, FactSearchResult.class))
                        .onErrorResume(exception -> {
                            log.warn("Error deserializando cache key={}", cacheKey, exception);
                            return Mono.empty();
                        }));
    }

    @Override
    public Mono<Void> put(String cacheKey, FactSearchResult result) {
        return Mono.fromCallable(() -> toJson(result))
                .flatMap(payload -> redisTemplate.opsForValue().set(cacheKey, payload, ttl))
                .then();
    }

    @Override
    public Mono<Void> evictTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.empty();
        }
        String pattern = tenantId + "::*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(500).build();
        return redisTemplate.scan(options)
                .collectList()
                .flatMap(keys -> keys.isEmpty() ? Mono.empty() : redisTemplate.delete(Flux.fromIterable(keys)).then());
    }

    private String toJson(FactSearchResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar FactSearchResult", exception);
        }
    }
}

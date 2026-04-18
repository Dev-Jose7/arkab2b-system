package com.arka.notification.infrastructure.adapter.out.cache;

import com.arka.notification.application.port.out.cache.NotificationSearchCachePort;
import com.arka.notification.application.result.NotificationSearchResult;
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
public class RedisNotificationSearchCacheAdapter implements NotificationSearchCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisNotificationSearchCacheAdapter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisNotificationSearchCacheAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.redis.cache.notification-search-ttl-seconds:30}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(Math.max(1L, ttlSeconds));
    }

    @Override
    public Mono<NotificationSearchResult> get(String cacheKey) {
        return redisTemplate
                .opsForValue()
                .get(cacheKey)
                .flatMap(payload -> Mono.fromCallable(() -> objectMapper.readValue(payload, NotificationSearchResult.class))
                        .onErrorResume(exception -> {
                            log.warn("Error deserializando cache key={}", cacheKey, exception);
                            return Mono.empty();
                        }));
    }

    @Override
    public Mono<Void> put(String cacheKey, NotificationSearchResult result) {
        return Mono.fromCallable(() -> toJson(result))
                .flatMap(payload -> redisTemplate.opsForValue().set(cacheKey, payload, ttl))
                .then();
    }

    @Override
    public Mono<Void> evictOrganization(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return Mono.empty();
        }
        String pattern = organizationId + "::*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(500).build();
        return redisTemplate.scan(options)
                .collectList()
                .flatMap(keys -> keys.isEmpty() ? Mono.empty() : redisTemplate.delete(Flux.fromIterable(keys)).then());
    }

    private String toJson(NotificationSearchResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar NotificationSearchResult", exception);
        }
    }
}

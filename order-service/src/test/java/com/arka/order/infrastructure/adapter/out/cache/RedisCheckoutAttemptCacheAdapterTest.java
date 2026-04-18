package com.arka.order.infrastructure.adapter.out.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.order.application.result.CheckoutAttemptResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RedisCheckoutAttemptCacheAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectProvider<MeterRegistry> meterRegistryProvider;

    @Test
    void shouldReturnAttemptOnCacheHit() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RedisCheckoutAttemptCacheAdapter adapter =
                new RedisCheckoutAttemptCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider, 300);

        CheckoutAttemptResult result = new CheckoutAttemptResult(
                "att-1",
                "corr-1",
                "tenant-1",
                "org-1",
                "user-1",
                "cart-1",
                "VALID",
                "addr-1",
                "CO",
                3L,
                "COP",
                List.of(),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:05:00Z"));
        String key = "order:checkout-attempt:tenant-1:corr-1";

        when(valueOperations.set(eq(key), any(String.class), eq(Duration.ofSeconds(300))))
                .thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(adapter.put(result)).verifyComplete();

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(key), payloadCaptor.capture(), eq(Duration.ofSeconds(300)));

        when(valueOperations.get(key)).thenReturn(Mono.just(payloadCaptor.getValue()));

        StepVerifier.create(adapter.findByCorrelation("tenant-1", "corr-1"))
                .assertNext(found -> {
                    assertEquals("att-1", found.checkoutAttemptId());
                    assertEquals("corr-1", found.checkoutCorrelationId());
                    assertEquals("tenant-1", found.tenantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenCacheMiss() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("order:checkout-attempt:tenant-1:corr-1")).thenReturn(Mono.empty());

        RedisCheckoutAttemptCacheAdapter adapter =
                new RedisCheckoutAttemptCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider, 300);

        StepVerifier.create(adapter.findByCorrelation("tenant-1", "corr-1")).verifyComplete();
    }

    @Test
    void shouldFallbackAndRecordMetricWhenRedisReadFails() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("order:checkout-attempt:tenant-1:corr-1"))
                .thenReturn(Mono.error(new RuntimeException("redis-down")));

        RedisCheckoutAttemptCacheAdapter adapter =
                new RedisCheckoutAttemptCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider, 300);

        StepVerifier.create(adapter.findByCorrelation("tenant-1", "corr-1")).verifyComplete();

        assertEquals(
                1.0,
                meterRegistry
                        .counter("order.cache.redis.failures", "cache", "checkout-attempt", "operation", "findByCorrelation")
                        .count(),
                0.0001);
    }
}

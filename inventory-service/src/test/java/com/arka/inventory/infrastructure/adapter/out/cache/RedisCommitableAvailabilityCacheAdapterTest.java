package com.arka.inventory.infrastructure.adapter.out.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.inventory.application.result.CommitableAvailabilityResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
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
class RedisCommitableAvailabilityCacheAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectProvider<MeterRegistry> meterRegistryProvider;

    @Test
    void shouldReturnAvailabilityOnCacheHit() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RedisCommitableAvailabilityCacheAdapter adapter =
                new RedisCommitableAvailabilityCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider, 300);

        CommitableAvailabilityResult result =
                new CommitableAvailabilityResult("organization-1", "wh-1", "sku-1", 10, 3, 7, 2, 1, false);
        String key = "inventory:availability:organization-1:wh-1:SKU-1";

        when(valueOperations.set(eq(key), any(String.class), eq(Duration.ofSeconds(300))))
                .thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(adapter.put(result)).verifyComplete();

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(key), payloadCaptor.capture(), eq(Duration.ofSeconds(300)));

        when(valueOperations.get(key)).thenReturn(Mono.just(payloadCaptor.getValue()));

        StepVerifier.create(adapter.find("organization-1", "wh-1", "sku-1"))
                .assertNext(found -> {
                    assertEquals("organization-1", found.organizationId());
                    assertEquals("wh-1", found.warehouseId());
                    assertEquals("sku-1", found.sku());
                    assertEquals(7, found.availableQty());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenCacheMiss() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("inventory:availability:organization-1:wh-1:SKU-1")).thenReturn(Mono.empty());

        RedisCommitableAvailabilityCacheAdapter adapter =
                new RedisCommitableAvailabilityCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider, 300);

        StepVerifier.create(adapter.find("organization-1", "wh-1", "sku-1")).verifyComplete();
    }

    @Test
    void shouldFallbackAndRecordMetricWhenRedisReadFails() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("inventory:availability:organization-1:wh-1:SKU-1"))
                .thenReturn(Mono.error(new RuntimeException("redis-down")));

        RedisCommitableAvailabilityCacheAdapter adapter =
                new RedisCommitableAvailabilityCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider, 300);

        StepVerifier.create(adapter.find("organization-1", "wh-1", "sku-1")).verifyComplete();

        assertEquals(
                1.0,
                meterRegistry
                        .counter(
                                "inventory.cache.redis.failures",
                                "cache",
                                "commitable-availability",
                                "operation",
                                "find")
                        .count(),
                0.0001);
    }
}

package com.arka.directory.infrastructure.adapter.out.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.domain.countrypolicy.enumtype.WeekStartsOn;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
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
class RedisDirectoryPolicyCacheAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectProvider<MeterRegistry> meterRegistryProvider;

    @Test
    void shouldReturnPolicyOnCacheHit() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RedisDirectoryPolicyCacheAdapter adapter =
                new RedisDirectoryPolicyCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider);

        CountryPolicy policy = samplePolicy();
        String key = "directory:policy:org-1:CO";

        when(valueOperations.set(eq(key), any(String.class), eq(Duration.ofMinutes(15))))
                .thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(adapter.putActive(policy)).verifyComplete();

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(key), payloadCaptor.capture(), eq(Duration.ofMinutes(15)));

        when(valueOperations.get(key)).thenReturn(Mono.just(payloadCaptor.getValue()));

        StepVerifier.create(adapter.findActive("org-1", "co"))
                .assertNext(found -> {
                    assertEquals("org-1", found.organizationId().value());
                    assertEquals("CO", found.countryCode().value());
                    assertEquals(3L, found.policyVersion().value());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenCacheMiss() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("directory:policy:org-1:CO")).thenReturn(Mono.empty());

        RedisDirectoryPolicyCacheAdapter adapter =
                new RedisDirectoryPolicyCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider);

        StepVerifier.create(adapter.findActive("org-1", "co")).verifyComplete();
    }

    @Test
    void shouldFallbackAndRecordMetricWhenRedisReadFails() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("directory:policy:org-1:CO")).thenReturn(Mono.error(new RuntimeException("redis-down")));

        RedisDirectoryPolicyCacheAdapter adapter =
                new RedisDirectoryPolicyCacheAdapter(redisTemplate, objectMapper, meterRegistryProvider);

        StepVerifier.create(adapter.findActive("org-1", "co")).verifyComplete();

        assertEquals(
                1.0,
                meterRegistry
                        .counter(
                                "directory.cache.redis.failures",
                                "cache",
                                "directory-policy",
                                "operation",
                                "findActive")
                        .count(),
                0.0001);
    }

    private CountryPolicy samplePolicy() {
        Instant now = Instant.parse("2026-01-01T10:15:30Z");
        return CountryPolicy.rehydrate(
                "pol-1",
                OrganizationId.of("org-1"),
                CountryCode.of("CO"),
                PolicyVersion.of(3),
                "COP",
                WeekStartsOn.MONDAY,
                "17:00",
                "America/Bogota",
                30,
                true,
                now.minusSeconds(3600),
                null,
                CountryPolicyStatus.ACTIVE,
                now.minusSeconds(7200),
                now);
    }
}

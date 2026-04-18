package com.arka.order.infrastructure.adapter.out.cache;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InMemoryCheckoutAttemptCacheAdapterTest {

    private final InMemoryCheckoutAttemptCacheAdapter adapter = new InMemoryCheckoutAttemptCacheAdapter(120);

    @Test
    void shouldReturnEmptyWhenKeyMissing() {
        StepVerifier.create(adapter.findByCorrelation("organization-1", "corr-1"))
                .verifyComplete();
    }

    @Test
    void shouldRejectNullPut() {
        StepVerifier.create(adapter.put(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldCompleteEvict() {
        StepVerifier.create(adapter.evict("organization-1", "corr-1"))
                .verifyComplete();
    }
}

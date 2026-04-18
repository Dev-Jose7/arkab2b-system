package com.arka.inventory.infrastructure.adapter.out.cache;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InMemoryCommitableAvailabilityCacheAdapterTest {

    private final InMemoryCommitableAvailabilityCacheAdapter adapter =
            new InMemoryCommitableAvailabilityCacheAdapter(120);

    @Test
    void shouldReturnEmptyWhenKeyIsMissing() {
        StepVerifier.create(adapter.find("organization-1", "wh-1", "SKU-1"))
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
        StepVerifier.create(adapter.evict("organization-1", "wh-1", "SKU-1"))
                .verifyComplete();
    }
}

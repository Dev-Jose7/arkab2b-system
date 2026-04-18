package com.arka.directory.infrastructure.adapter.out.cache;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InMemoryDirectoryPolicyCacheAdapterTest {

    private final InMemoryDirectoryPolicyCacheAdapter adapter = new InMemoryDirectoryPolicyCacheAdapter(120);

    @Test
    void shouldReturnEmptyWhenEntryIsMissing() {
        StepVerifier.create(adapter.findActive("org-1", "CO"))
                .verifyComplete();
    }

    @Test
    void shouldRejectNullPolicyOnPut() {
        StepVerifier.create(adapter.putActive(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldCompleteEvictWithoutError() {
        StepVerifier.create(adapter.evictActive("org-1", "CO"))
                .verifyComplete();
    }
}

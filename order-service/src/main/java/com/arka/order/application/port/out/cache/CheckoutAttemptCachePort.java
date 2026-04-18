package com.arka.order.application.port.out.cache;

import com.arka.order.application.result.CheckoutAttemptResult;
import reactor.core.publisher.Mono;

public interface CheckoutAttemptCachePort {

    Mono<CheckoutAttemptResult> findByCorrelation(String tenantId, String checkoutCorrelationId);

    Mono<Void> put(CheckoutAttemptResult result);

    Mono<Void> evict(String tenantId, String checkoutCorrelationId);
}

package com.arka.order.application.port.out.persistence;

import com.arka.order.domain.cart.entity.CheckoutAttempt;
import reactor.core.publisher.Mono;

public interface CheckoutAttemptPersistencePort {

    Mono<CheckoutAttempt> save(CheckoutAttempt checkoutAttempt);

    Mono<CheckoutAttempt> findByCorrelation(String organizationId, String checkoutCorrelationId);
}

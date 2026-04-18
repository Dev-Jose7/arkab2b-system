package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.ResolveCheckoutAvailabilityQuery;
import com.arka.inventory.application.result.CheckoutAvailabilityResult;
import reactor.core.publisher.Mono;

public interface ResolveCheckoutAvailabilityQueryUseCase {

    Mono<CheckoutAvailabilityResult> handle(ResolveCheckoutAvailabilityQuery query);
}

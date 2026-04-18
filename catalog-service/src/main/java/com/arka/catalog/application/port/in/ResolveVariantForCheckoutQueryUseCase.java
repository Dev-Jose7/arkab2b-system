package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.ResolveVariantForCheckoutQuery;
import com.arka.catalog.application.result.CheckoutVariantResolutionResult;
import reactor.core.publisher.Mono;

public interface ResolveVariantForCheckoutQueryUseCase {

    Mono<CheckoutVariantResolutionResult> handle(ResolveVariantForCheckoutQuery query);
}

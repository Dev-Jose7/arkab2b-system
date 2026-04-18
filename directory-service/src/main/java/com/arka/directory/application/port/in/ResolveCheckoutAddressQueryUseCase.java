package com.arka.directory.application.port.in;

import com.arka.directory.application.query.ResolveCheckoutAddressQuery;
import com.arka.directory.application.result.CheckoutAddressResolutionResult;
import reactor.core.publisher.Mono;

public interface ResolveCheckoutAddressQueryUseCase {

    Mono<CheckoutAddressResolutionResult> handle(ResolveCheckoutAddressQuery query);
}

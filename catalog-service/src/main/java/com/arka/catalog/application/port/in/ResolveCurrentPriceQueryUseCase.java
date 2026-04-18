package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.ResolveCurrentPriceQuery;
import com.arka.catalog.application.result.PriceResult;
import reactor.core.publisher.Mono;

public interface ResolveCurrentPriceQueryUseCase {

    Mono<PriceResult> handle(ResolveCurrentPriceQuery query);
}

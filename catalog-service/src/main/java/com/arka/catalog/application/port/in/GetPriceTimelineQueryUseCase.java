package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.GetPriceTimelineQuery;
import com.arka.catalog.application.result.PriceTimelineResult;
import reactor.core.publisher.Mono;

public interface GetPriceTimelineQueryUseCase {

    Mono<PriceTimelineResult> handle(GetPriceTimelineQuery query);
}

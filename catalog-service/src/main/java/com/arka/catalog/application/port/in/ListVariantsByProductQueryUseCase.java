package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.ListVariantsByProductQuery;
import com.arka.catalog.application.result.VariantResult;
import reactor.core.publisher.Flux;

public interface ListVariantsByProductQueryUseCase {

    Flux<VariantResult> handle(ListVariantsByProductQuery query);
}

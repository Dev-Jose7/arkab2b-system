package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.GetProductByIdQuery;
import com.arka.catalog.application.result.ProductResult;
import reactor.core.publisher.Mono;

public interface GetProductByIdQueryUseCase {

    Mono<ProductResult> handle(GetProductByIdQuery query);
}

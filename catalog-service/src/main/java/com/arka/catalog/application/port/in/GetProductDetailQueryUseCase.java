package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.GetProductDetailQuery;
import com.arka.catalog.application.result.ProductDetailResult;
import reactor.core.publisher.Mono;

public interface GetProductDetailQueryUseCase {

    Mono<ProductDetailResult> handle(GetProductDetailQuery query);
}

package com.arka.order.application.port.in;

import com.arka.order.application.query.GetCartQuery;
import com.arka.order.application.result.CartResult;
import reactor.core.publisher.Mono;

public interface GetCartQueryUseCase {

    Mono<CartResult> handle(GetCartQuery query);
}

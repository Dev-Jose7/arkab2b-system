package com.arka.order.application.port.in;

import com.arka.order.application.query.GetActiveCartQuery;
import com.arka.order.application.result.CartResult;
import reactor.core.publisher.Mono;

public interface GetActiveCartQueryUseCase {

    Mono<CartResult> handle(GetActiveCartQuery query);
}

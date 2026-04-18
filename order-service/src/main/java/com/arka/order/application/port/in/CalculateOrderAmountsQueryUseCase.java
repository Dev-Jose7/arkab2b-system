package com.arka.order.application.port.in;

import com.arka.order.application.query.CalculateOrderAmountsQuery;
import com.arka.order.application.result.OrderAmountsResult;
import reactor.core.publisher.Mono;

public interface CalculateOrderAmountsQueryUseCase {

    Mono<OrderAmountsResult> handle(CalculateOrderAmountsQuery query);
}

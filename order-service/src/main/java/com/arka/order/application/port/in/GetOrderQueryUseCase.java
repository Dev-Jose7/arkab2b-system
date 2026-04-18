package com.arka.order.application.port.in;

import com.arka.order.application.query.GetOrderQuery;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface GetOrderQueryUseCase {

    Mono<OrderResult> handle(GetOrderQuery query);
}

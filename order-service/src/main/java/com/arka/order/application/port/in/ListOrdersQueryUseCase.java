package com.arka.order.application.port.in;

import com.arka.order.application.query.ListOrdersQuery;
import com.arka.order.application.result.OrderSummaryResult;
import reactor.core.publisher.Flux;

public interface ListOrdersQueryUseCase {

    Flux<OrderSummaryResult> handle(ListOrdersQuery query);
}

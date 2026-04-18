package com.arka.order.application.port.in;

import com.arka.order.application.query.GetOrderTimelineQuery;
import com.arka.order.application.result.OrderStatusHistoryResult;
import reactor.core.publisher.Flux;

public interface GetOrderTimelineQueryUseCase {

    Flux<OrderStatusHistoryResult> handle(GetOrderTimelineQuery query);
}

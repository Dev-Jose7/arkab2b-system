package com.arka.order.application.port.in;

import com.arka.order.application.query.GetOrderFinancialStatusQuery;
import com.arka.order.application.result.OrderFinancialStatusResult;
import reactor.core.publisher.Mono;

public interface GetOrderFinancialStatusQueryUseCase {

    Mono<OrderFinancialStatusResult> handle(GetOrderFinancialStatusQuery query);
}

package com.arka.order.application.port.in;

import com.arka.order.application.command.RevalidateOrderConsistencyAfterAdjustmentCommand;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface RevalidateOrderConsistencyAfterAdjustmentCommandUseCase {

    Mono<OrderResult> handle(RevalidateOrderConsistencyAfterAdjustmentCommand command);
}

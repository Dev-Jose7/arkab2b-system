package com.arka.order.application.port.in;

import com.arka.order.application.command.CancelOrderCommand;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface CancelOrderCommandUseCase {

    Mono<OrderResult> handle(CancelOrderCommand command);
}

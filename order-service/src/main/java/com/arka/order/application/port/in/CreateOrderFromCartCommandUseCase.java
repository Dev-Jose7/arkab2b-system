package com.arka.order.application.port.in;

import com.arka.order.application.command.CreateOrderFromCartCommand;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface CreateOrderFromCartCommandUseCase {

    Mono<OrderResult> handle(CreateOrderFromCartCommand command);
}

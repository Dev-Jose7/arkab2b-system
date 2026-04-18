package com.arka.order.application.port.in;

import com.arka.order.application.command.UpdateOrderOperationalStatusCommand;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface UpdateOrderOperationalStatusCommandUseCase {

    Mono<OrderResult> handle(UpdateOrderOperationalStatusCommand command);
}

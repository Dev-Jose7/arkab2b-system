package com.arka.order.application.port.in;

import com.arka.order.application.command.CreateCartCommand;
import com.arka.order.application.result.CartResult;
import reactor.core.publisher.Mono;

public interface CreateCartCommandUseCase {

    Mono<CartResult> handle(CreateCartCommand command);
}

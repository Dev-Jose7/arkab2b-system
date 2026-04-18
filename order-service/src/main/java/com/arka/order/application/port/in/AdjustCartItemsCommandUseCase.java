package com.arka.order.application.port.in;

import com.arka.order.application.command.AdjustCartItemsCommand;
import com.arka.order.application.result.CartResult;
import reactor.core.publisher.Mono;

public interface AdjustCartItemsCommandUseCase {

    Mono<CartResult> handle(AdjustCartItemsCommand command);
}

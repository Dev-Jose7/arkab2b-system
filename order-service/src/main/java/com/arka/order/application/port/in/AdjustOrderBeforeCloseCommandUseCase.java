package com.arka.order.application.port.in;

import com.arka.order.application.command.AdjustOrderBeforeCloseCommand;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface AdjustOrderBeforeCloseCommandUseCase {

    Mono<OrderResult> handle(AdjustOrderBeforeCloseCommand command);
}

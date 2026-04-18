package com.arka.order.application.port.in;

import com.arka.order.application.command.RegisterManualPaymentCommand;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface RegisterManualPaymentCommandUseCase {

    Mono<OrderResult> handle(RegisterManualPaymentCommand command);
}

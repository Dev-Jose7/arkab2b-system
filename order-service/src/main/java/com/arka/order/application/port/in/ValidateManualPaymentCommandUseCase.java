package com.arka.order.application.port.in;

import com.arka.order.application.command.ValidateManualPaymentCommand;
import com.arka.order.application.result.OrderResult;
import reactor.core.publisher.Mono;

public interface ValidateManualPaymentCommandUseCase {

    Mono<OrderResult> handle(ValidateManualPaymentCommand command);
}

package com.arka.order.application.port.in;

import com.arka.order.application.command.ValidateCheckoutAvailabilityCommand;
import com.arka.order.application.result.CheckoutAttemptResult;
import reactor.core.publisher.Mono;

public interface ValidateCheckoutAvailabilityCommandUseCase {

    Mono<CheckoutAttemptResult> handle(ValidateCheckoutAvailabilityCommand command);
}

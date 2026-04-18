package com.arka.order.application.port.in;

import com.arka.order.application.command.HandleReservationExpiredCommand;
import com.arka.order.application.result.CartResult;
import reactor.core.publisher.Mono;

public interface HandleReservationExpiredCommandUseCase {

    Mono<CartResult> handle(HandleReservationExpiredCommand command);
}

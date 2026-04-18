package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.ExpireReservationsCommand;
import com.arka.inventory.application.result.ExpiredReservationsResult;
import reactor.core.publisher.Mono;

public interface ExpireReservationsCommandUseCase {

    Mono<ExpiredReservationsResult> handle(ExpireReservationsCommand command);
}

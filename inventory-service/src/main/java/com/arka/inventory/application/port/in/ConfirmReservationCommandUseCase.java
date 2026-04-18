package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.ConfirmReservationCommand;
import com.arka.inventory.application.result.StockReservationResult;
import reactor.core.publisher.Mono;

public interface ConfirmReservationCommandUseCase {

    Mono<StockReservationResult> handle(ConfirmReservationCommand command);
}

package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.ReleaseReservationCommand;
import com.arka.inventory.application.result.StockReservationResult;
import reactor.core.publisher.Mono;

public interface ReleaseReservationCommandUseCase {

    Mono<StockReservationResult> handle(ReleaseReservationCommand command);
}

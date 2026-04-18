package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.ReserveStockCommand;
import com.arka.inventory.application.result.StockReservationResult;
import reactor.core.publisher.Mono;

public interface ReserveStockCommandUseCase {

    Mono<StockReservationResult> handle(ReserveStockCommand command);
}

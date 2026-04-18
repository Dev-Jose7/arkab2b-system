package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.UpdateStockItemStatusCommand;
import com.arka.inventory.application.result.StockItemResult;
import reactor.core.publisher.Mono;

public interface UpdateStockItemStatusCommandUseCase {

    Mono<StockItemResult> handle(UpdateStockItemStatusCommand command);
}

package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.UpdateOperationalStockCommand;
import com.arka.inventory.application.result.StockItemResult;
import reactor.core.publisher.Mono;

public interface UpdateOperationalStockCommandUseCase {

    Mono<StockItemResult> handle(UpdateOperationalStockCommand command);
}

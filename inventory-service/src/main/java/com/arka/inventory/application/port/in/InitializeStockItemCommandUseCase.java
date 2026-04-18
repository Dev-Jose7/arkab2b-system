package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.InitializeStockItemCommand;
import com.arka.inventory.application.result.StockItemResult;
import reactor.core.publisher.Mono;

public interface InitializeStockItemCommandUseCase {

    Mono<StockItemResult> handle(InitializeStockItemCommand command);
}

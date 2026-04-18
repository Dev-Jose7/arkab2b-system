package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.GetStockMovementsQuery;
import com.arka.inventory.application.result.StockMovementResult;
import reactor.core.publisher.Flux;

public interface GetStockMovementsQueryUseCase {

    Flux<StockMovementResult> handle(GetStockMovementsQuery query);
}

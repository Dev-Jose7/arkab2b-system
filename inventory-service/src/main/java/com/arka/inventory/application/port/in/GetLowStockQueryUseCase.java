package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.GetLowStockQuery;
import com.arka.inventory.application.result.StockItemResult;
import reactor.core.publisher.Flux;

public interface GetLowStockQueryUseCase {

    Flux<StockItemResult> handle(GetLowStockQuery query);
}

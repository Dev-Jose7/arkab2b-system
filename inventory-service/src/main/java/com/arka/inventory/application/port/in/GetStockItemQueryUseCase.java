package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.GetStockItemQuery;
import com.arka.inventory.application.result.StockItemResult;
import reactor.core.publisher.Mono;

public interface GetStockItemQueryUseCase {

    Mono<StockItemResult> handle(GetStockItemQuery query);
}

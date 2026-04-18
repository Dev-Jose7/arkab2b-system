package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.ListStockByWarehouseQuery;
import com.arka.inventory.application.result.StockItemResult;
import reactor.core.publisher.Flux;

public interface ListStockByWarehouseQueryUseCase {

    Flux<StockItemResult> handle(ListStockByWarehouseQuery query);
}

package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.ListReservationsByCartQuery;
import com.arka.inventory.application.result.StockReservationResult;
import reactor.core.publisher.Flux;

public interface ListReservationsByCartQueryUseCase {

    Flux<StockReservationResult> handle(ListReservationsByCartQuery query);
}

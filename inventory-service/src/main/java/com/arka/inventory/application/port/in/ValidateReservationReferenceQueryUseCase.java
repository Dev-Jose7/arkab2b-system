package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.ValidateReservationReferenceQuery;
import com.arka.inventory.application.result.ReservationValidationResult;
import reactor.core.publisher.Mono;

public interface ValidateReservationReferenceQueryUseCase {

    Mono<ReservationValidationResult> handle(ValidateReservationReferenceQuery query);
}

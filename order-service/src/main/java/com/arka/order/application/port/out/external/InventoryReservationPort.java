package com.arka.order.application.port.out.external;

import reactor.core.publisher.Mono;

public interface InventoryReservationPort {

    Mono<InventoryReservationValidation> validateReservation(String tenantId, String reservationId, String sku, int qty);
}

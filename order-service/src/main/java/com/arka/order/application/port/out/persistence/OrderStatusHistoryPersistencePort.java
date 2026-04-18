package com.arka.order.application.port.out.persistence;

import com.arka.order.domain.order.entity.OrderStatusHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderStatusHistoryPersistencePort {

    Mono<OrderStatusHistory> save(OrderStatusHistory statusHistory);

    Flux<OrderStatusHistory> findByOrder(String organizationId, String orderId);
}

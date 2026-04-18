package com.arka.order.application.port.out.persistence;

import com.arka.order.domain.order.aggregate.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PurchaseOrderPersistencePort {

    Mono<Boolean> existsByCheckoutCorrelation(String tenantId, String checkoutCorrelationId);

    Mono<Order> findByCheckoutCorrelation(String tenantId, String checkoutCorrelationId);

    Mono<Order> findById(String tenantId, String orderId);

    Mono<Order> save(Order order);

    Mono<Boolean> updateWithExpectedVersion(Order order, long expectedVersion);

    Flux<Order> listByTenantOrganizationStatus(
            String tenantId,
            String organizationId,
            String status,
            java.time.Instant createdFrom,
            java.time.Instant createdTo,
            int limit);
}

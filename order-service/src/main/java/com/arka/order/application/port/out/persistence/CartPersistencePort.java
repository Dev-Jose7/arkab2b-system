package com.arka.order.application.port.out.persistence;

import com.arka.order.domain.cart.aggregate.Cart;
import reactor.core.publisher.Mono;

public interface CartPersistencePort {

    Mono<Cart> findById(String tenantId, String cartId);

    Mono<Cart> findActiveByTenantOrganizationUser(String tenantId, String organizationId, String userId);

    Mono<Cart> save(Cart cart);

    Mono<Boolean> updateWithExpectedVersion(Cart cart, long expectedVersion);
}

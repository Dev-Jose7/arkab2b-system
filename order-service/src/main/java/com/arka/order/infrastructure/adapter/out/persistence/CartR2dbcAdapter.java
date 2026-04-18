package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.application.port.out.persistence.CartPersistencePort;
import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.infrastructure.adapter.out.persistence.entity.CartEntity;
import com.arka.order.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.CartPersistenceMapper;
import com.arka.order.infrastructure.adapter.out.persistence.repository.CartItemR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.CartR2dbcRepository;
import java.util.List;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CartR2dbcAdapter implements CartPersistencePort {

    private final CartR2dbcRepository cartRepository;
    private final CartItemR2dbcRepository cartItemRepository;
    private final CartPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    public CartR2dbcAdapter(
            CartR2dbcRepository cartRepository,
            CartItemR2dbcRepository cartItemRepository,
            CartPersistenceMapper mapper,
            R2dbcEntityTemplate entityTemplate) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.mapper = mapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Cart> findById(String tenantId, String cartId) {
        return cartRepository.findByTenantAndCartId(tenantId, cartId)
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Cart> findActiveByTenantOrganizationUser(String tenantId, String organizationId, String userId) {
        return cartRepository.findActiveByTenantOrganizationUser(tenantId, organizationId, userId)
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Cart> save(Cart cart) {
        CartEntity entity = mapper.toEntity(cart);
        List<CartItemEntity> items = mapper.toItemEntities(cart);
        return cartRepository.existsById(entity.cartId())
                .flatMap(exists -> exists ? cartRepository.save(entity) : entityTemplate.insert(entity))
                .then(replaceItems(cart.cartId(), items))
                .thenReturn(cart);
    }

    @Override
    public Mono<Boolean> updateWithExpectedVersion(Cart cart, long expectedVersion) {
        return cartRepository
                .updateWithExpectedVersion(
                        cart.tenantId(),
                        cart.cartId(),
                        cart.status().name(),
                        cart.version(),
                        expectedVersion,
                        cart.updatedAt())
                .flatMap(updatedRows -> {
                    if (updatedRows == null || updatedRows <= 0) {
                        return Mono.just(Boolean.FALSE);
                    }
                    return replaceItems(cart.cartId(), mapper.toItemEntities(cart))
                            .thenReturn(Boolean.TRUE);
                });
    }

    private Mono<Cart> toDomain(CartEntity cartEntity) {
        return cartItemRepository.findByCartId(cartEntity.cartId())
                .collectList()
                .map(items -> mapper.toDomain(cartEntity, items));
    }

    private Mono<Void> replaceItems(String cartId, List<CartItemEntity> items) {
        return cartItemRepository.deleteByCartId(cartId)
                .thenMany(items == null || items.isEmpty()
                        ? reactor.core.publisher.Flux.empty()
                        : reactor.core.publisher.Flux.fromIterable(items)
                                .concatMap(entityTemplate::insert))
                .then();
    }
}

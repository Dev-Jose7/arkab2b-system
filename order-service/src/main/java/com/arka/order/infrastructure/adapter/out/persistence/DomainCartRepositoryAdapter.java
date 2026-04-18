package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.domain.cart.repository.CartRepository;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.CartPersistenceMapper;
import com.arka.order.infrastructure.adapter.out.persistence.repository.CartItemR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.CartR2dbcRepository;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DomainCartRepositoryAdapter implements CartRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final CartR2dbcRepository cartRepository;
    private final CartItemR2dbcRepository cartItemRepository;
    private final CartPersistenceMapper mapper;

    public DomainCartRepositoryAdapter(
            CartR2dbcRepository cartRepository,
            CartItemR2dbcRepository cartItemRepository,
            CartPersistenceMapper mapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Cart> findById(String cartId) {
        return cartRepository
                .findById(cartId)
                .flatMap(entity -> cartItemRepository.findByCartId(entity.cartId())
                        .collectList()
                        .map(items -> mapper.toDomain(entity, items)))
                .blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public Cart save(Cart cart) {
        Cart persisted = cartRepository
                .save(mapper.toEntity(cart))
                .flatMap(saved -> cartItemRepository.deleteByCartId(saved.cartId())
                        .thenMany(cartItemRepository.saveAll(mapper.toItemEntities(cart)))
                        .then(cartItemRepository.findByCartId(saved.cartId()).collectList())
                        .map(items -> mapper.toDomain(saved, items)))
                .block(BLOCK_TIMEOUT);
        if (persisted == null) {
            throw new IllegalStateException("Cart persistence returned empty result");
        }
        return persisted;
    }
}

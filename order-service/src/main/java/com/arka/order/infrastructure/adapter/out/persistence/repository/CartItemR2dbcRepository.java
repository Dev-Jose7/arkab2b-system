package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartItemR2dbcRepository extends ReactiveCrudRepository<CartItemEntity, String> {

    @Query("""
            SELECT *
            FROM cart_items
            WHERE cart_id = :cartId
            ORDER BY created_at ASC
            """)
    Flux<CartItemEntity> findByCartId(String cartId);

    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_id = :cartId")
    Mono<Integer> deleteByCartId(String cartId);
}

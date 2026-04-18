package com.arka.order.domain.cart.repository;

import com.arka.order.domain.cart.aggregate.Cart;
import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findById(String cartId);

    Cart save(Cart cart);
}

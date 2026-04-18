package com.arka.order.domain.cart.service;

import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.domain.cart.entity.CartItem;
import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import org.springframework.stereotype.Component;

@Component
public class CartPolicyService {

    public void ensureOwnership(Cart cart, String organizationId, String userId) {
        if (cart == null) {
            throw new DomainInvariantViolationException("cart is required");
        }
        if (!cart.organizationId().equals(organizationId)) {
            throw new DomainInvariantViolationException("organization isolation violated for cart");
        }
        if (!cart.userId().equals(userId)) {
            throw new DomainInvariantViolationException("user ownership violated for cart");
        }
    }

    public void ensureReservationReferences(CartItem item) {
        if (item == null) {
            throw new DomainInvariantViolationException("item is required");
        }
        item.ensureReservationConfirmed();
    }
}

package com.arka.order.infrastructure.adapter.out.persistence.mapper;

import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.domain.cart.entity.CartItem;
import com.arka.order.domain.cart.enumtype.CartStatus;
import com.arka.order.infrastructure.adapter.out.persistence.entity.CartEntity;
import com.arka.order.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CartPersistenceMapper {

    public CartEntity toEntity(Cart cart) {
        return new CartEntity(
                cart.cartId(),
                cart.tenantId(),
                cart.organizationId(),
                cart.userId(),
                cart.status().name(),
                cart.version(),
                cart.createdAt(),
                cart.updatedAt());
    }

    public List<CartItemEntity> toItemEntities(Cart cart) {
        return cart.items().stream().map(this::toItemEntity).toList();
    }

    public CartItemEntity toItemEntity(CartItem item) {
        return new CartItemEntity(
                item.cartItemId(),
                item.cartId(),
                item.tenantId(),
                item.organizationId(),
                item.variantId(),
                item.sku(),
                item.qty(),
                item.unitPrice(),
                item.currency(),
                item.reservationId(),
                item.reservationConfirmed(),
                item.createdAt(),
                item.updatedAt());
    }

    public Cart toDomain(CartEntity cart, List<CartItemEntity> items) {
        return Cart.rehydrate(
                cart.cartId(),
                cart.tenantId(),
                cart.organizationId(),
                cart.userId(),
                CartStatus.valueOf(cart.status()),
                cart.version(),
                cart.createdAt(),
                cart.updatedAt(),
                items == null ? List.of() : items.stream().map(this::toDomain).toList());
    }

    public CartItem toDomain(CartItemEntity item) {
        return new CartItem(
                item.cartItemId(),
                item.cartId(),
                item.tenantId(),
                item.organizationId(),
                item.variantId(),
                item.sku(),
                item.qty(),
                item.unitPrice(),
                item.currency(),
                item.reservationId(),
                item.reservationConfirmed(),
                item.createdAt(),
                item.updatedAt());
    }
}

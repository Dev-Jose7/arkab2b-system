package com.arka.order.domain.order.repository;

import com.arka.order.domain.order.aggregate.Order;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(String orderId);

    Order save(Order order);
}

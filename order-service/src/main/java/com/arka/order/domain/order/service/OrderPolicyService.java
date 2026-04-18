package com.arka.order.domain.order.service;

import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.domain.order.enumtype.OrderStatus;
import com.arka.order.domain.order.exception.InvalidOrderTransitionException;
import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import org.springframework.stereotype.Component;

@Component
public class OrderPolicyService {

    public void ensureOwnership(Order order, String tenantId, String organizationId) {
        if (order == null) {
            throw new DomainInvariantViolationException("order is required");
        }
        if (!order.tenantId().equals(tenantId)) {
            throw new DomainInvariantViolationException("tenant isolation violated for order");
        }
        if (!order.organizationId().equals(organizationId)) {
            throw new DomainInvariantViolationException("organization isolation violated for order");
        }
    }

    public void ensureMvpTransition(OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new InvalidOrderTransitionException("target status is required");
        }
        if (targetStatus == OrderStatus.READY_TO_DISPATCH
                || targetStatus == OrderStatus.DISPATCHED
                || targetStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderTransitionException("target status is reserved for future baseline");
        }
    }
}

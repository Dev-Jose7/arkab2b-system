package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResponse(
        String cartId,
        String tenantId,
        String organizationId,
        String userId,
        String status,
        BigDecimal subtotal,
        long version,
        Instant createdAt,
        Instant updatedAt,
        List<CartItemResponse> items) {}

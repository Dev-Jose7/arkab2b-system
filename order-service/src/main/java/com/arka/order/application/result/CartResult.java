package com.arka.order.application.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResult(
        String cartId,
        String tenantId,
        String organizationId,
        String userId,
        String status,
        BigDecimal subtotal,
        long version,
        Instant createdAt,
        Instant updatedAt,
        List<CartItemResult> items) {}

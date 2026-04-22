package com.arka.order.infrastructure.adapter.in.web.response;

import java.time.Instant;
import java.util.List;

public record AbandonedCartResponse(
        String cartId,
        String organizationId,
        String userId,
        String status,
        boolean inferredAbandoned,
        Instant createdAt,
        Instant updatedAt,
        List<AbandonedCartItemResponse> items) {
}

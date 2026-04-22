package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record FrequentCustomerResponse(
        String userId,
        long orderCount,
        BigDecimal totalSpent) {
}

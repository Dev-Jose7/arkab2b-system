package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record WeeklySalesSummaryResponse(
        String organizationId,
        String weekId,
        Instant from,
        Instant to,
        BigDecimal totalSales,
        long totalOrders,
        List<WeeklySalesTopProductResponse> topProducts,
        List<FrequentCustomerResponse> frequentCustomers) {
}

package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record WeeklySalesTopProductResponse(
        String sku,
        long totalQty,
        BigDecimal totalSales) {
}

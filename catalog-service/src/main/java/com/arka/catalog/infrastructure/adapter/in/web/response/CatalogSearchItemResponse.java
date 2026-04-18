package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record CatalogSearchItemResponse(
        String productId,
        String productCode,
        String productName,
        String variantId,
        String sku,
        String variantStatus,
        BigDecimal amount,
        String currency,
        String priceType,
        boolean sellable) {
}

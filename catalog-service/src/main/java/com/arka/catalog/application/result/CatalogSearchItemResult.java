package com.arka.catalog.application.result;

import java.math.BigDecimal;

public record CatalogSearchItemResult(
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

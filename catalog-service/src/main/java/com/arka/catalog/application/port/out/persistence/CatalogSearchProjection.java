package com.arka.catalog.application.port.out.persistence;

import java.math.BigDecimal;

public record CatalogSearchProjection(
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

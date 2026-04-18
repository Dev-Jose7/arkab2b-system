package com.arka.order.application.port.out.external;

import java.math.BigDecimal;

public record CatalogVariantSnapshot(
        String variantId,
        String sku,
        BigDecimal unitPrice,
        String currency,
        boolean sellable) {}

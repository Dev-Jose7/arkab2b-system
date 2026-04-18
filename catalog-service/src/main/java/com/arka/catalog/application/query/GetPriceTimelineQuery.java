package com.arka.catalog.application.query;

public record GetPriceTimelineQuery(
        String tenantId,
        String variantId,
        String currency,
        String priceType) {
}

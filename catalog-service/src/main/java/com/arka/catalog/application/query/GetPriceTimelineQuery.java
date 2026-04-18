package com.arka.catalog.application.query;

public record GetPriceTimelineQuery(
        String organizationId,
        String variantId,
        String currency,
        String priceType) {
}

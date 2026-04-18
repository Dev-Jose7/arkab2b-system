package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.util.List;

public record PriceTimelineResponse(
        String variantId,
        String currency,
        String priceType,
        List<PriceResponse> timeline) {
}

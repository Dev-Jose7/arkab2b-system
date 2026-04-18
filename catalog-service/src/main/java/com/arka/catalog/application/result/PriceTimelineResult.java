package com.arka.catalog.application.result;

import java.util.List;

public record PriceTimelineResult(
        String variantId,
        String currency,
        String priceType,
        List<PriceResult> timeline) {
}

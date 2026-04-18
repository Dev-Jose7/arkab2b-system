package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.util.List;

public record ProductDetailResponse(
        ProductResponse product,
        List<VariantResponse> variants,
        List<PriceResponse> activePrices) {
}

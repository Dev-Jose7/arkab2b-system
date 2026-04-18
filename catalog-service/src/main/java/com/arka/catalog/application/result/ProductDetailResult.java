package com.arka.catalog.application.result;

import java.util.List;

public record ProductDetailResult(
        ProductResult product,
        List<VariantResult> variants,
        List<PriceResult> activePrices) {
}

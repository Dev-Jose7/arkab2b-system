package com.arka.catalog.domain.catalogoffer.service;

import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class PriceResolutionPolicy {

    private PriceResolutionPolicy() {
    }

    public static Optional<Price> resolveActivePrice(List<Price> timeline, PriceType priceType, Instant at) {
        if (timeline == null || timeline.isEmpty()) {
            return Optional.empty();
        }
        return timeline.stream()
                .filter(price -> price.priceType() == priceType)
                .filter(price -> price.isActiveAt(at))
                .max(Comparator.comparing(price -> price.timeWindow().effectiveFrom()));
    }
}

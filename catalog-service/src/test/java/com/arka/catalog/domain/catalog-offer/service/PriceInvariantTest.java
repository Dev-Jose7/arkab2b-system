package com.arka.catalog.domain.catalogoffer.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.exception.PricePeriodOverlapException;
import com.arka.catalog.domain.catalogoffer.valueobject.Money;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.OrganizationId;
import com.arka.catalog.domain.catalogoffer.valueobject.TimeWindow;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceInvariantTest {

    @Test
    void shouldRejectOverlappingPricePeriodsForSameVariantCurrencyAndType() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        OrganizationId organizationId = OrganizationId.of("organization-demo");
        VariantId variantId = VariantId.of("variant-1");

        Price existing = Price.register(
                organizationId,
                PriceId.of("price-existing"),
                variantId,
                PriceType.BASE,
                Money.of(new BigDecimal("10.00"), "COP"),
                TimeWindow.of(now, now.plusSeconds(3600)),
                now);

        Price candidate = Price.register(
                organizationId,
                PriceId.of("price-new"),
                variantId,
                PriceType.BASE,
                Money.of(new BigDecimal("11.00"), "COP"),
                TimeWindow.of(now.plusSeconds(1800), now.plusSeconds(7200)),
                now);

        assertThrows(PricePeriodOverlapException.class, () -> Price.ensureNoOverlap(candidate, List.of(existing)));
    }

    @Test
    void shouldAllowNonOverlappingPricePeriodsForSameVariantCurrencyAndType() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        OrganizationId organizationId = OrganizationId.of("organization-demo");
        VariantId variantId = VariantId.of("variant-1");

        Price existing = Price.register(
                organizationId,
                PriceId.of("price-existing"),
                variantId,
                PriceType.BASE,
                Money.of(new BigDecimal("10.00"), "COP"),
                TimeWindow.of(now, now.plusSeconds(3600)),
                now);

        Price candidate = Price.register(
                organizationId,
                PriceId.of("price-new"),
                variantId,
                PriceType.BASE,
                Money.of(new BigDecimal("11.00"), "COP"),
                TimeWindow.of(now.plusSeconds(3600), now.plusSeconds(7200)),
                now);

        assertDoesNotThrow(() -> Price.ensureNoOverlap(candidate, List.of(existing)));
    }
}

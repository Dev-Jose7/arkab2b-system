package com.arka.reporting.domain.analyticfact.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.reporting.domain.analyticfact.aggregate.AnalyticFactAggregate;
import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactType;
import com.arka.reporting.domain.analyticfact.exception.FactAlreadyAppliedException;
import com.arka.reporting.domain.analyticfact.exception.InvalidAnalyticFactTransitionException;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.analyticfact.valueobject.TenantId;
import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AnalyticFactDomainModelTest {

    @Test
    void shouldNormalizeAndApplyFactOnce() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        AnalyticFact fact = AnalyticFact.capture(
                TenantId.of("tenant-demo"),
                SourceEventId.of("evt-1"),
                "order.confirmed",
                AnalyticFactType.SALES,
                "{\"amount\":120.5}",
                now,
                now);

        AnalyticFactAggregate aggregate = AnalyticFactAggregate.rehydrate(fact);
        aggregate.normalize("{\"amount\":120.50}", now.plusSeconds(1));
        aggregate.apply(now.plusSeconds(2));

        assertEquals("APPLIED", aggregate.fact().factStatus().name());
        assertEquals(1, aggregate.pullDomainEvents().size());
    }

    @Test
    void shouldRejectSecondApplyForSameFact() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        AnalyticFact fact = AnalyticFact.capture(
                TenantId.of("tenant-demo"),
                SourceEventId.of("evt-2"),
                "order.confirmed",
                AnalyticFactType.SALES,
                "{\"amount\":40}",
                now,
                now);

        AnalyticFactAggregate aggregate = AnalyticFactAggregate.rehydrate(fact);
        aggregate.normalize("{\"amount\":40}", now.plusSeconds(1));
        aggregate.apply(now.plusSeconds(2));

        assertThrows(FactAlreadyAppliedException.class, () -> aggregate.apply(now.plusSeconds(3)));
    }

    @Test
    void shouldNotAllowRejectAfterApplied() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        AnalyticFact fact = AnalyticFact.capture(
                TenantId.of("tenant-demo"),
                SourceEventId.of("evt-3"),
                "inventory.low-stock",
                AnalyticFactType.REPLENISHMENT,
                "{\"sku\":\"SKU-1\"}",
                now,
                now);

        AnalyticFactAggregate aggregate = AnalyticFactAggregate.rehydrate(fact);
        aggregate.normalize("{\"sku\":\"SKU-1\"}", now.plusSeconds(1));
        aggregate.apply(now.plusSeconds(2));

        assertThrows(
                InvalidAnalyticFactTransitionException.class,
                () -> aggregate.reject("invalid", now.plusSeconds(3)));
    }

    @Test
    void shouldRejectApplyWhenFactIsNotNormalized() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        AnalyticFact fact = AnalyticFact.capture(
                TenantId.of("tenant-demo"),
                SourceEventId.of("evt-4"),
                "notification.delivery",
                AnalyticFactType.NOTIFICATION,
                "{\"delivery\":true}",
                now,
                now);

        AnalyticFactAggregate aggregate = AnalyticFactAggregate.rehydrate(fact);

        assertThrows(DomainInvariantViolationException.class, () -> aggregate.apply(now.plusSeconds(1)));
    }
}

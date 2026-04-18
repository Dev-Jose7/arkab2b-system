package com.arka.catalog.domain.catalogoffer.entity;

import com.arka.catalog.domain.catalogoffer.enumtype.PriceStatus;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.exception.PricePeriodOverlapException;
import com.arka.catalog.domain.catalogoffer.valueobject.Money;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.OrganizationId;
import com.arka.catalog.domain.catalogoffer.valueobject.TimeWindow;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import java.time.Instant;
import java.util.Collection;

public final class Price {

    private final OrganizationId organizationId;
    private final PriceId priceId;
    private final VariantId variantId;
    private final Instant createdAt;

    private PriceType priceType;
    private Money money;
    private TimeWindow timeWindow;
    private PriceStatus status;
    private Instant updatedAt;

    private Price(
            OrganizationId organizationId,
            PriceId priceId,
            VariantId variantId,
            PriceType priceType,
            Money money,
            TimeWindow timeWindow,
            PriceStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.organizationId = organizationId;
        this.priceId = priceId;
        this.variantId = variantId;
        this.priceType = priceType;
        this.money = money;
        this.timeWindow = timeWindow;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Price register(
            OrganizationId organizationId,
            PriceId priceId,
            VariantId variantId,
            PriceType priceType,
            Money money,
            TimeWindow timeWindow,
            Instant now) {
        Price price = new Price(
                organizationId,
                priceId,
                variantId,
                priceType,
                money,
                timeWindow,
                PriceStatus.SCHEDULED,
                now,
                now);
        price.refreshLifecycle(now);
        return price;
    }

    public static Price rehydrate(
            OrganizationId organizationId,
            PriceId priceId,
            VariantId variantId,
            PriceType priceType,
            Money money,
            TimeWindow timeWindow,
            PriceStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new Price(
                organizationId,
                priceId,
                variantId,
                priceType,
                money,
                timeWindow,
                status,
                createdAt,
                updatedAt);
    }

    public void update(Money money, TimeWindow window, Instant now) {
        this.money = money;
        this.timeWindow = window;
        refreshLifecycle(now);
        this.updatedAt = now;
    }

    public void refreshLifecycle(Instant now) {
        if (timeWindow.contains(now)) {
            this.status = PriceStatus.ACTIVE;
            return;
        }
        if (now.isBefore(timeWindow.effectiveFrom())) {
            this.status = PriceStatus.SCHEDULED;
            return;
        }
        this.status = PriceStatus.EXPIRED;
    }

    public boolean isActiveAt(Instant instant) {
        return timeWindow.contains(instant);
    }

    public boolean overlaps(Price other) {
        if (!variantId.value().equals(other.variantId.value())) {
            return false;
        }
        if (!money.currency().equals(other.money.currency())) {
            return false;
        }
        if (priceType != other.priceType) {
            return false;
        }
        return timeWindow.overlaps(other.timeWindow);
    }

    public static void ensureNoOverlap(Price candidate, Collection<Price> existing) {
        for (Price current : existing) {
            if (candidate.priceId.value().equals(current.priceId.value())) {
                continue;
            }
            if (candidate.overlaps(current)) {
                throw new PricePeriodOverlapException();
            }
        }
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public PriceId priceId() {
        return priceId;
    }

    public VariantId variantId() {
        return variantId;
    }

    public PriceType priceType() {
        return priceType;
    }

    public Money money() {
        return money;
    }

    public TimeWindow timeWindow() {
        return timeWindow;
    }

    public PriceStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

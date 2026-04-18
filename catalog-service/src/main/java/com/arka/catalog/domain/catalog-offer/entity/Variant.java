package com.arka.catalog.domain.catalogoffer.entity;

import com.arka.catalog.domain.catalogoffer.enumtype.ProductStatus;
import com.arka.catalog.domain.catalogoffer.enumtype.VariantStatus;
import com.arka.catalog.domain.catalogoffer.exception.ProductNotActiveException;
import com.arka.catalog.domain.catalogoffer.exception.RequiredAttributesMissingException;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public final class Variant {

    private final TenantId tenantId;
    private final VariantId variantId;
    private final ProductId productId;
    private final Instant createdAt;

    private String sku;
    private String name;
    private String description;
    private VariantStatus status;
    private Instant sellableFrom;
    private Instant sellableUntil;
    private Integer weightGrams;
    private Instant updatedAt;

    private Variant(
            TenantId tenantId,
            VariantId variantId,
            ProductId productId,
            String sku,
            String name,
            String description,
            VariantStatus status,
            Instant sellableFrom,
            Instant sellableUntil,
            Integer weightGrams,
            Instant createdAt,
            Instant updatedAt) {
        this.tenantId = tenantId;
        this.variantId = variantId;
        this.productId = productId;
        this.sku = requireNotBlank(sku, "sku").toUpperCase();
        this.name = requireNotBlank(name, "name");
        this.description = description == null ? "" : description.trim();
        this.status = status;
        this.sellableFrom = sellableFrom;
        this.sellableUntil = sellableUntil;
        this.weightGrams = weightGrams;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        ensureSellableWindowConsistency();
    }

    public static Variant draft(
            TenantId tenantId,
            VariantId variantId,
            ProductId productId,
            String sku,
            String name,
            String description,
            Integer weightGrams,
            Instant now) {
        return new Variant(
                tenantId,
                variantId,
                productId,
                sku,
                name,
                description,
                VariantStatus.DRAFT,
                null,
                null,
                weightGrams,
                now,
                now);
    }

    public static Variant rehydrate(
            TenantId tenantId,
            VariantId variantId,
            ProductId productId,
            String sku,
            String name,
            String description,
            VariantStatus status,
            Instant sellableFrom,
            Instant sellableUntil,
            Integer weightGrams,
            Instant createdAt,
            Instant updatedAt) {
        return new Variant(
                tenantId,
                variantId,
                productId,
                sku,
                name,
                description,
                status,
                sellableFrom,
                sellableUntil,
                weightGrams,
                createdAt,
                updatedAt);
    }

    public void update(String name, String description, Integer weightGrams, Instant now) {
        if (status == VariantStatus.DISCONTINUED) {
            throw new DomainInvariantViolationException(
                    "variante_descontinuada",
                    "No se puede editar una variante descontinuada");
        }
        this.name = requireNotBlank(name, "name");
        this.description = description == null ? "" : description.trim();
        this.weightGrams = weightGrams;
        this.updatedAt = now;
    }

    public void markSellable(
            ProductStatus productStatus,
            boolean hasRequiredAttributes,
            Instant sellableFrom,
            Instant sellableUntil,
            Instant now) {
        if (productStatus != ProductStatus.ACTIVE) {
            throw new ProductNotActiveException();
        }
        if (!hasRequiredAttributes) {
            throw new RequiredAttributesMissingException();
        }
        this.status = VariantStatus.SELLABLE;
        this.sellableFrom = sellableFrom == null ? now : sellableFrom;
        this.sellableUntil = sellableUntil;
        this.updatedAt = now;
        ensureSellableWindowConsistency();
    }

    public void discontinue(Instant now) {
        this.status = VariantStatus.DISCONTINUED;
        this.sellableUntil = now;
        this.updatedAt = now;
    }

    public boolean isSellableAt(Instant instant) {
        if (status != VariantStatus.SELLABLE) {
            return false;
        }
        Instant from = sellableFrom == null ? Instant.MIN : sellableFrom;
        Instant until = sellableUntil == null ? Instant.MAX : sellableUntil;
        return !instant.isBefore(from) && instant.isBefore(until);
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public VariantId variantId() {
        return variantId;
    }

    public ProductId productId() {
        return productId;
    }

    public String sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public VariantStatus status() {
        return status;
    }

    public Instant sellableFrom() {
        return sellableFrom;
    }

    public Instant sellableUntil() {
        return sellableUntil;
    }

    public Integer weightGrams() {
        return weightGrams;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private void ensureSellableWindowConsistency() {
        if (sellableFrom != null && sellableUntil != null && !sellableUntil.isAfter(sellableFrom)) {
            throw new DomainInvariantViolationException(
                    "variante_invalida",
                    "sellableUntil debe ser mayor que sellableFrom");
        }
    }

    private String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("variante_invalida", field + " es obligatorio");
        }
        return value.trim();
    }
}

package com.arka.catalog.domain.catalogoffer.entity;

import com.arka.catalog.domain.catalogoffer.enumtype.ProductStatus;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.OrganizationId;
import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public final class Product {

    private final OrganizationId organizationId;
    private final ProductId productId;
    private final Instant createdAt;

    private String productCode;
    private String name;
    private String description;
    private String brandId;
    private String categoryId;
    private ProductStatus status;
    private Instant updatedAt;

    private Product(
            OrganizationId organizationId,
            ProductId productId,
            String productCode,
            String name,
            String description,
            String brandId,
            String categoryId,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.organizationId = organizationId;
        this.productId = productId;
        this.productCode = requireNotBlank(productCode, "productCode").toUpperCase();
        this.name = requireNotBlank(name, "name");
        this.description = description == null ? "" : description.trim();
        this.brandId = requireNotBlank(brandId, "brandId");
        this.categoryId = requireNotBlank(categoryId, "categoryId");
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product draft(
            OrganizationId organizationId,
            ProductId productId,
            String productCode,
            String name,
            String description,
            String brandId,
            String categoryId,
            Instant now) {
        return new Product(
                organizationId,
                productId,
                productCode,
                name,
                description,
                brandId,
                categoryId,
                ProductStatus.DRAFT,
                now,
                now);
    }

    public static Product rehydrate(
            OrganizationId organizationId,
            ProductId productId,
            String productCode,
            String name,
            String description,
            String brandId,
            String categoryId,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new Product(
                organizationId,
                productId,
                productCode,
                name,
                description,
                brandId,
                categoryId,
                status,
                createdAt,
                updatedAt);
    }

    public void update(
            String name,
            String description,
            String brandId,
            String categoryId,
            Instant now) {
        if (status == ProductStatus.RETIRED) {
            throw new DomainInvariantViolationException(
                    "producto_retirado",
                    "No se puede editar un producto retirado");
        }
        this.name = requireNotBlank(name, "name");
        this.description = description == null ? "" : description.trim();
        this.brandId = requireNotBlank(brandId, "brandId");
        this.categoryId = requireNotBlank(categoryId, "categoryId");
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        if (status == ProductStatus.RETIRED) {
            throw new DomainInvariantViolationException(
                    "producto_retirado",
                    "No se puede activar un producto retirado");
        }
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void retire(Instant now) {
        this.status = ProductStatus.RETIRED;
        this.updatedAt = now;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public ProductId productId() {
        return productId;
    }

    public String productCode() {
        return productCode;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String brandId() {
        return brandId;
    }

    public String categoryId() {
        return categoryId;
    }

    public ProductStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("producto_invalido", field + " es obligatorio");
        }
        return value.trim();
    }
}

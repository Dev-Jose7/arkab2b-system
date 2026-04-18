package com.arka.catalog.infrastructure.adapter.out.persistence.mapper;

import com.arka.catalog.application.port.out.audit.CatalogAuditEntry;
import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.entity.PriceSchedule;
import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.ProductTag;
import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceScheduleJobStatus;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceStatus;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.enumtype.ProductStatus;
import com.arka.catalog.domain.catalogoffer.enumtype.VariantStatus;
import com.arka.catalog.domain.catalogoffer.valueobject.Money;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.TimeWindow;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.CatalogAuditRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.PriceRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.PriceScheduleRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.ProductRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.ProductTagRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.VariantAttributeRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.VariantRow;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CatalogRowMapper {

    public ProductRow toRow(Product product) {
        return new ProductRow(
                product.productId().value(),
                product.tenantId().value(),
                product.productCode(),
                product.name(),
                product.description(),
                product.brandId(),
                product.categoryId(),
                product.status().name(),
                product.createdAt(),
                product.updatedAt());
    }

    public Product toDomain(ProductRow row) {
        return Product.rehydrate(
                TenantId.of(row.tenantId()),
                ProductId.of(row.productId()),
                row.productCode(),
                row.productName(),
                row.description(),
                row.brandId(),
                row.categoryId(),
                ProductStatus.valueOf(row.status()),
                row.createdAt(),
                row.updatedAt());
    }

    public ProductTagRow toRow(String tenantId, String productId, ProductTag tag, Instant now) {
        return new ProductTagRow(
                UUID.randomUUID().toString(),
                tenantId,
                productId,
                tag.tagCode(),
                tag.tagValue(),
                now,
                now);
    }

    public ProductTag toDomain(ProductTagRow row) {
        return new ProductTag(row.tagCode(), row.tagValue());
    }

    public VariantRow toRow(Variant variant) {
        return new VariantRow(
                variant.variantId().value(),
                variant.tenantId().value(),
                variant.productId().value(),
                variant.sku(),
                variant.name(),
                variant.description(),
                variant.status().name(),
                variant.sellableFrom(),
                variant.sellableUntil(),
                variant.weightGrams(),
                variant.createdAt(),
                variant.updatedAt());
    }

    public Variant toDomain(VariantRow row) {
        return Variant.rehydrate(
                TenantId.of(row.tenantId()),
                VariantId.of(row.variantId()),
                ProductId.of(row.productId()),
                row.sku(),
                row.variantName(),
                row.description(),
                VariantStatus.valueOf(row.status()),
                row.sellableFrom(),
                row.sellableUntil(),
                row.weightGrams(),
                row.createdAt(),
                row.updatedAt());
    }

    public VariantAttributeRow toRow(String tenantId, String variantId, VariantAttribute attribute, Instant now) {
        return new VariantAttributeRow(
                UUID.randomUUID().toString(),
                tenantId,
                variantId,
                attribute.attributeCode(),
                attribute.value(),
                attribute.normalizedValue(),
                now,
                now);
    }

    public VariantAttribute toDomain(VariantAttributeRow row) {
        return new VariantAttribute(row.attributeCode(), row.attributeValue(), row.normalizedValue());
    }

    public PriceRow toRow(Price price) {
        return new PriceRow(
                price.priceId().value(),
                price.tenantId().value(),
                price.variantId().value(),
                price.priceType().name(),
                price.money().currency(),
                price.money().amount(),
                price.timeWindow().effectiveFrom(),
                price.timeWindow().effectiveUntil(),
                price.status().name(),
                price.createdAt(),
                price.updatedAt());
    }

    public Price toDomain(PriceRow row) {
        return Price.rehydrate(
                TenantId.of(row.tenantId()),
                PriceId.of(row.priceId()),
                VariantId.of(row.variantId()),
                PriceType.valueOf(row.priceType()),
                Money.of(row.amount(), row.currency()),
                TimeWindow.of(row.effectiveFrom(), row.effectiveUntil()),
                PriceStatus.valueOf(row.status()),
                row.createdAt(),
                row.updatedAt());
    }

    public PriceScheduleRow toRow(PriceSchedule schedule) {
        return new PriceScheduleRow(
                schedule.scheduleId(),
                schedule.tenantId(),
                schedule.priceId(),
                schedule.executeAfter(),
                schedule.jobStatus().name(),
                schedule.errorMessage(),
                schedule.createdAt(),
                schedule.updatedAt());
    }

    public PriceSchedule toDomain(PriceScheduleRow row) {
        return new PriceSchedule(
                row.scheduleId(),
                row.tenantId(),
                row.priceId(),
                row.executeAfter(),
                PriceScheduleJobStatus.valueOf(row.jobStatus()),
                row.errorMessage(),
                row.createdAt(),
                row.updatedAt());
    }

    public CatalogAuditRow toRow(CatalogAuditEntry entry) {
        return new CatalogAuditRow(
                entry.auditId(),
                entry.tenantId(),
                entry.actorId(),
                entry.actionType(),
                entry.targetType(),
                entry.targetId(),
                entry.outcome(),
                entry.payload(),
                entry.idempotencyKey(),
                entry.payloadHash(),
                entry.createdAt());
    }

    public CatalogAuditEntry toDomain(CatalogAuditRow row) {
        return new CatalogAuditEntry(
                row.auditId(),
                row.tenantId(),
                row.actorId(),
                row.actionType(),
                row.targetType(),
                row.targetId(),
                row.outcome(),
                row.payload(),
                row.idempotencyKey(),
                row.payloadHash(),
                row.createdAt());
    }
}

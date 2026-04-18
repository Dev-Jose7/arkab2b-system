package com.arka.catalog.application.mapper.result;

import com.arka.catalog.application.result.CatalogAuditEntryResult;
import com.arka.catalog.application.result.CatalogOfferResult;
import com.arka.catalog.application.result.CatalogSearchItemResult;
import com.arka.catalog.application.result.PriceResult;
import com.arka.catalog.application.result.ProductResult;
import com.arka.catalog.application.result.VariantAttributeResult;
import com.arka.catalog.application.result.VariantResult;
import com.arka.catalog.application.port.out.audit.CatalogAuditEntry;
import com.arka.catalog.application.port.out.persistence.CatalogSearchProjection;
import com.arka.catalog.domain.catalogoffer.aggregate.CatalogOffer;
import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.ProductTag;
import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogResultMapper {

    public ProductResult toProductResult(Product product, List<ProductTag> tags) {
        return new ProductResult(
                product.productId().value(),
                product.organizationId().value(),
                product.productCode(),
                product.name(),
                product.description(),
                product.brandId(),
                product.categoryId(),
                product.status().name(),
                tags.stream().map(ProductTag::tagValue).toList(),
                product.createdAt(),
                product.updatedAt());
    }

    public VariantResult toVariantResult(Variant variant, List<VariantAttribute> attributes) {
        return new VariantResult(
                variant.variantId().value(),
                variant.organizationId().value(),
                variant.productId().value(),
                variant.sku(),
                variant.name(),
                variant.description(),
                variant.status().name(),
                variant.sellableFrom(),
                variant.sellableUntil(),
                variant.weightGrams(),
                attributes.stream()
                        .map(attr -> new VariantAttributeResult(
                                attr.attributeCode(),
                                attr.value(),
                                attr.normalizedValue()))
                        .toList(),
                variant.createdAt(),
                variant.updatedAt());
    }

    public PriceResult toPriceResult(Price price) {
        return new PriceResult(
                price.priceId().value(),
                price.organizationId().value(),
                price.variantId().value(),
                price.priceType().name(),
                price.money().amount(),
                price.money().currency(),
                price.status().name(),
                price.timeWindow().effectiveFrom(),
                price.timeWindow().effectiveUntil(),
                price.createdAt(),
                price.updatedAt());
    }

    public CatalogOfferResult toOfferResult(CatalogOffer offer) {
        return new CatalogOfferResult(
                offer.offerId().value(),
                offer.organizationId().value(),
                offer.product().productId().value(),
                offer.variant().variantId().value(),
                offer.price().priceId().value(),
                offer.regionalPolicyReference(),
                offer.publishedAt(),
                offer.updatedAt());
    }

    public CatalogAuditEntryResult toAuditEntryResult(CatalogAuditEntry entry) {
        return new CatalogAuditEntryResult(
                entry.auditId(),
                entry.organizationId(),
                entry.actorId(),
                entry.actionType(),
                entry.targetType(),
                entry.targetId(),
                entry.outcome(),
                entry.payload(),
                entry.idempotencyKey(),
                entry.createdAt());
    }

    public CatalogSearchItemResult toSearchItemResult(CatalogSearchProjection projection) {
        return new CatalogSearchItemResult(
                projection.productId(),
                projection.productCode(),
                projection.productName(),
                projection.variantId(),
                projection.sku(),
                projection.variantStatus(),
                projection.amount(),
                projection.currency(),
                projection.priceType(),
                projection.sellable());
    }
}

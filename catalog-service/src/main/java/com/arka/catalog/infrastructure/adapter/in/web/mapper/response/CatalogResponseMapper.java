package com.arka.catalog.infrastructure.adapter.in.web.mapper.response;

import com.arka.catalog.application.result.CatalogAuditEntryResult;
import com.arka.catalog.application.result.CatalogAuditResult;
import com.arka.catalog.application.result.CatalogOfferResult;
import com.arka.catalog.application.result.CatalogSearchItemResult;
import com.arka.catalog.application.result.CatalogSearchResult;
import com.arka.catalog.application.result.CheckoutVariantResolutionResult;
import com.arka.catalog.application.result.PriceResult;
import com.arka.catalog.application.result.PriceTimelineResult;
import com.arka.catalog.application.result.ProductDetailResult;
import com.arka.catalog.application.result.ProductResult;
import com.arka.catalog.application.result.VariantAttributeResult;
import com.arka.catalog.application.result.VariantResult;
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogAuditEntryResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogAuditResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogOfferResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogSearchItemResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogSearchResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CheckoutVariantResolutionResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.PriceResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.PriceTimelineResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.ProductDetailResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.ProductResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.VariantAttributeResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.VariantResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogResponseMapper {

    public ProductResponse toResponse(ProductResult result) {
        return new ProductResponse(
                result.productId(),
                result.tenantId(),
                result.productCode(),
                result.name(),
                result.description(),
                result.brandId(),
                result.categoryId(),
                result.status(),
                result.tags(),
                result.createdAt(),
                result.updatedAt());
    }

    public VariantResponse toResponse(VariantResult result) {
        List<VariantAttributeResponse> attributes = result.attributes().stream().map(this::toResponse).toList();
        return new VariantResponse(
                result.variantId(),
                result.tenantId(),
                result.productId(),
                result.sku(),
                result.name(),
                result.description(),
                result.status(),
                result.sellableFrom(),
                result.sellableUntil(),
                result.weightGrams(),
                attributes,
                result.createdAt(),
                result.updatedAt());
    }

    public VariantAttributeResponse toResponse(VariantAttributeResult result) {
        return new VariantAttributeResponse(result.attributeCode(), result.value(), result.normalizedValue());
    }

    public PriceResponse toResponse(PriceResult result) {
        return new PriceResponse(
                result.priceId(),
                result.tenantId(),
                result.variantId(),
                result.priceType(),
                result.amount(),
                result.currency(),
                result.status(),
                result.effectiveFrom(),
                result.effectiveUntil(),
                result.createdAt(),
                result.updatedAt());
    }

    public CatalogOfferResponse toResponse(CatalogOfferResult result) {
        return new CatalogOfferResponse(
                result.offerId(),
                result.tenantId(),
                result.productId(),
                result.variantId(),
                result.priceId(),
                result.regionalPolicyReference(),
                result.publishedAt(),
                result.updatedAt());
    }

    public ProductDetailResponse toResponse(ProductDetailResult result) {
        return new ProductDetailResponse(
                toResponse(result.product()),
                result.variants().stream().map(this::toResponse).toList(),
                result.activePrices().stream().map(this::toResponse).toList());
    }

    public CatalogSearchResponse toResponse(CatalogSearchResult result) {
        return new CatalogSearchResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements());
    }

    public CatalogSearchItemResponse toResponse(CatalogSearchItemResult result) {
        return new CatalogSearchItemResponse(
                result.productId(),
                result.productCode(),
                result.productName(),
                result.variantId(),
                result.sku(),
                result.variantStatus(),
                result.amount(),
                result.currency(),
                result.priceType(),
                result.sellable());
    }

    public CheckoutVariantResolutionResponse toResponse(CheckoutVariantResolutionResult result) {
        return new CheckoutVariantResolutionResponse(
                result.tenantId(),
                result.productId(),
                result.variantId(),
                result.sku(),
                result.priceId(),
                result.amount(),
                result.currency(),
                result.priceType(),
                result.resolvedAt());
    }

    public PriceTimelineResponse toResponse(PriceTimelineResult result) {
        return new PriceTimelineResponse(
                result.variantId(),
                result.currency(),
                result.priceType(),
                result.timeline().stream().map(this::toResponse).toList());
    }

    public CatalogAuditResponse toResponse(CatalogAuditResult result) {
        return new CatalogAuditResponse(
                result.entries().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements());
    }

    public CatalogAuditEntryResponse toResponse(CatalogAuditEntryResult result) {
        return new CatalogAuditEntryResponse(
                result.auditId(),
                result.tenantId(),
                result.actorId(),
                result.actionType(),
                result.targetType(),
                result.targetId(),
                result.outcome(),
                result.payload(),
                result.idempotencyKey(),
                result.createdAt());
    }
}

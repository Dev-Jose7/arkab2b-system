package com.arka.catalog.infrastructure.adapter.in.web.mapper.command;

import com.arka.catalog.application.command.ActivateProductCommand;
import com.arka.catalog.application.command.ChangeVariantStatusCommand;
import com.arka.catalog.application.command.CreateProductCommand;
import com.arka.catalog.application.command.CreateVariantCommand;
import com.arka.catalog.application.command.PublishCatalogOfferCommand;
import com.arka.catalog.application.command.RegisterPriceCommand;
import com.arka.catalog.application.command.RetireProductCommand;
import com.arka.catalog.application.command.SchedulePriceActivationCommand;
import com.arka.catalog.application.command.UpdateCatalogOfferCommand;
import com.arka.catalog.application.command.UpdatePriceCommand;
import com.arka.catalog.application.command.UpdateProductCommand;
import com.arka.catalog.application.command.UpdateVariantCommand;
import com.arka.catalog.application.command.UpsertVariantAttributesCommand;
import com.arka.catalog.application.command.VariantAttributeInput;
import com.arka.catalog.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.catalog.infrastructure.adapter.in.web.request.ChangeVariantStatusRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.CreateProductRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.CreateVariantRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.PublishCatalogOfferRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.RegisterPriceRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.SchedulePriceActivationRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.UpdateCatalogOfferRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.UpdatePriceRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.UpdateProductRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.UpdateVariantRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.UpsertVariantAttributesRequest;
import com.arka.catalog.infrastructure.adapter.in.web.request.VariantAttributeRequest;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogCommandMapper {

    public CreateProductCommand toCommand(CreateProductRequest request, IamSecurityPrincipal principal) {
        return new CreateProductCommand(
                principal.organizationId(),
                principal.actorId(),
                request.productCode(),
                request.name(),
                request.description(),
                request.brandId(),
                request.categoryId(),
                request.tags(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public UpdateProductCommand toCommand(String productId, UpdateProductRequest request, IamSecurityPrincipal principal) {
        return new UpdateProductCommand(
                principal.organizationId(),
                principal.actorId(),
                productId,
                request.name(),
                request.description(),
                request.brandId(),
                request.categoryId(),
                request.tags(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public ActivateProductCommand toActivateCommand(String productId, String idempotencyKey, IamSecurityPrincipal principal) {
        return new ActivateProductCommand(
                principal.organizationId(),
                principal.actorId(),
                productId,
                normalizeIdempotencyKey(idempotencyKey));
    }

    public RetireProductCommand toRetireCommand(String productId, String idempotencyKey, IamSecurityPrincipal principal) {
        return new RetireProductCommand(
                principal.organizationId(),
                principal.actorId(),
                productId,
                normalizeIdempotencyKey(idempotencyKey));
    }

    public CreateVariantCommand toCommand(String productId, CreateVariantRequest request, IamSecurityPrincipal principal) {
        return new CreateVariantCommand(
                principal.organizationId(),
                principal.actorId(),
                productId,
                request.sku(),
                request.name(),
                request.description(),
                request.weightGrams(),
                toVariantAttributes(request.attributes()),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public UpdateVariantCommand toCommand(String variantId, UpdateVariantRequest request, IamSecurityPrincipal principal) {
        return new UpdateVariantCommand(
                principal.organizationId(),
                principal.actorId(),
                variantId,
                request.name(),
                request.description(),
                request.weightGrams(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public ChangeVariantStatusCommand toCommand(String variantId, ChangeVariantStatusRequest request, IamSecurityPrincipal principal) {
        return new ChangeVariantStatusCommand(
                principal.organizationId(),
                principal.actorId(),
                variantId,
                request.targetStatus(),
                request.sellableFrom(),
                request.sellableUntil(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public UpsertVariantAttributesCommand toCommand(
            String variantId,
            UpsertVariantAttributesRequest request,
            IamSecurityPrincipal principal) {
        return new UpsertVariantAttributesCommand(
                principal.organizationId(),
                principal.actorId(),
                variantId,
                toVariantAttributes(request.attributes()),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public RegisterPriceCommand toCommand(String variantId, RegisterPriceRequest request, IamSecurityPrincipal principal) {
        return new RegisterPriceCommand(
                principal.organizationId(),
                principal.actorId(),
                variantId,
                request.amount(),
                request.currency(),
                request.priceType(),
                request.effectiveFrom(),
                request.effectiveUntil(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public UpdatePriceCommand toCommand(String priceId, UpdatePriceRequest request, IamSecurityPrincipal principal) {
        return new UpdatePriceCommand(
                principal.organizationId(),
                principal.actorId(),
                priceId,
                request.amount(),
                request.currency(),
                request.priceType(),
                request.effectiveFrom(),
                request.effectiveUntil(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public SchedulePriceActivationCommand toCommand(
            String priceId,
            SchedulePriceActivationRequest request,
            IamSecurityPrincipal principal) {
        return new SchedulePriceActivationCommand(
                principal.organizationId(),
                principal.actorId(),
                priceId,
                request.executeAfter(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public PublishCatalogOfferCommand toCommand(PublishCatalogOfferRequest request, IamSecurityPrincipal principal) {
        return new PublishCatalogOfferCommand(
                principal.organizationId(),
                principal.actorId(),
                request.productId(),
                request.variantId(),
                request.priceId(),
                request.regionalPolicyReference(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public UpdateCatalogOfferCommand toCommand(
            String offerId,
            UpdateCatalogOfferRequest request,
            IamSecurityPrincipal principal) {
        return new UpdateCatalogOfferCommand(
                principal.organizationId(),
                principal.actorId(),
                offerId,
                request.variantId(),
                request.priceId(),
                request.regionalPolicyReference(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    private List<VariantAttributeInput> toVariantAttributes(List<VariantAttributeRequest> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream()
                .map(attribute -> new VariantAttributeInput(
                        attribute.attributeCode(),
                        attribute.value(),
                        attribute.normalizedValue()))
                .toList();
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        return normalized.isBlank() ? null : normalized;
    }
}

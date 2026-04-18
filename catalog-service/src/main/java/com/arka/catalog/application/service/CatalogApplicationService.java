package com.arka.catalog.application.service;

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
import com.arka.catalog.application.exception.ActorNotLegitimateException;
import com.arka.catalog.application.exception.ApplicationException;
import com.arka.catalog.application.exception.CatalogResourceNotFoundException;
import com.arka.catalog.application.exception.IdempotencyConflictException;
import com.arka.catalog.application.mapper.command.IdempotencySupport;
import com.arka.catalog.application.mapper.result.CatalogResultMapper;
import com.arka.catalog.application.port.in.ActivateProductCommandUseCase;
import com.arka.catalog.application.port.in.ChangeVariantStatusCommandUseCase;
import com.arka.catalog.application.port.in.CreateProductCommandUseCase;
import com.arka.catalog.application.port.in.CreateVariantCommandUseCase;
import com.arka.catalog.application.port.in.GetCatalogAuditQueryUseCase;
import com.arka.catalog.application.port.in.GetPriceTimelineQueryUseCase;
import com.arka.catalog.application.port.in.GetProductByIdQueryUseCase;
import com.arka.catalog.application.port.in.GetProductDetailQueryUseCase;
import com.arka.catalog.application.port.in.ListVariantsByProductQueryUseCase;
import com.arka.catalog.application.port.in.PublishCatalogOfferCommandUseCase;
import com.arka.catalog.application.port.in.RegisterPriceCommandUseCase;
import com.arka.catalog.application.port.in.ResolveCurrentPriceQueryUseCase;
import com.arka.catalog.application.port.in.ResolveVariantForCheckoutQueryUseCase;
import com.arka.catalog.application.port.in.RetireProductCommandUseCase;
import com.arka.catalog.application.port.in.SchedulePriceActivationCommandUseCase;
import com.arka.catalog.application.port.in.SearchCatalogQueryUseCase;
import com.arka.catalog.application.port.in.UpdateCatalogOfferCommandUseCase;
import com.arka.catalog.application.port.in.UpdatePriceCommandUseCase;
import com.arka.catalog.application.port.in.UpdateProductCommandUseCase;
import com.arka.catalog.application.port.in.UpdateVariantCommandUseCase;
import com.arka.catalog.application.port.in.UpsertVariantAttributesCommandUseCase;
import com.arka.catalog.application.port.out.audit.CatalogAuditEntry;
import com.arka.catalog.application.port.out.audit.CatalogAuditPort;
import com.arka.catalog.application.port.out.cache.CatalogSearchCachePort;
import com.arka.catalog.application.port.out.directory.RegionalPolicyContext;
import com.arka.catalog.application.port.out.directory.RegionalPolicyContextPort;
import com.arka.catalog.application.port.out.event.DomainEventTopicPort;
import com.arka.catalog.application.port.out.external.ActorLegitimacyPort;
import com.arka.catalog.application.port.out.external.ClockPort;
import com.arka.catalog.application.port.out.persistence.CatalogOfferPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogPricePersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogProductPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogReadPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogSearchFilter;
import com.arka.catalog.application.port.out.persistence.CatalogSearchProjection;
import com.arka.catalog.application.port.out.persistence.CatalogTaxonomyPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogVariantPersistencePort;
import com.arka.catalog.application.port.out.persistence.OutboxPersistencePort;
import com.arka.catalog.application.port.out.persistence.PriceSchedulePersistencePort;
import com.arka.catalog.application.port.out.security.ActorContext;
import com.arka.catalog.application.port.out.security.ActorContextProviderPort;
import com.arka.catalog.application.query.GetCatalogAuditQuery;
import com.arka.catalog.application.query.GetPriceTimelineQuery;
import com.arka.catalog.application.query.GetProductByIdQuery;
import com.arka.catalog.application.query.GetProductDetailQuery;
import com.arka.catalog.application.query.ListVariantsByProductQuery;
import com.arka.catalog.application.query.ResolveCurrentPriceQuery;
import com.arka.catalog.application.query.ResolveVariantForCheckoutQuery;
import com.arka.catalog.application.query.SearchCatalogQuery;
import com.arka.catalog.application.result.CatalogAuditResult;
import com.arka.catalog.application.result.CatalogOfferResult;
import com.arka.catalog.application.result.CatalogSearchItemResult;
import com.arka.catalog.application.result.CatalogSearchResult;
import com.arka.catalog.application.result.CheckoutVariantResolutionResult;
import com.arka.catalog.application.result.PriceResult;
import com.arka.catalog.application.result.PriceTimelineResult;
import com.arka.catalog.application.result.ProductDetailResult;
import com.arka.catalog.application.result.ProductResult;
import com.arka.catalog.application.result.VariantResult;
import com.arka.catalog.domain.catalogoffer.aggregate.CatalogOffer;
import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.entity.PriceSchedule;
import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.ProductTag;
import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceScheduleJobStatus;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.enumtype.VariantStatus;
import com.arka.catalog.domain.catalogoffer.event.CatalogMutationEvent;
import com.arka.catalog.domain.catalogoffer.exception.BrandOrCategoryInvalidException;
import com.arka.catalog.domain.catalogoffer.exception.CrossTenantAccessException;
import com.arka.catalog.domain.catalogoffer.exception.SkuNotUniqueException;
import com.arka.catalog.domain.catalogoffer.exception.VariantNotSellableException;
import com.arka.catalog.domain.catalogoffer.service.CatalogOfferPolicy;
import com.arka.catalog.domain.catalogoffer.valueobject.Money;
import com.arka.catalog.domain.catalogoffer.valueobject.OfferId;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.TimeWindow;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import com.arka.catalog.domain.shared.event.DomainEvent;
import com.arka.catalog.domain.shared.exception.OperationNotPermittedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CatalogApplicationService
        implements CreateProductCommandUseCase,
                UpdateProductCommandUseCase,
                ActivateProductCommandUseCase,
                RetireProductCommandUseCase,
                CreateVariantCommandUseCase,
                UpdateVariantCommandUseCase,
                ChangeVariantStatusCommandUseCase,
                UpsertVariantAttributesCommandUseCase,
                RegisterPriceCommandUseCase,
                UpdatePriceCommandUseCase,
                SchedulePriceActivationCommandUseCase,
                PublishCatalogOfferCommandUseCase,
                UpdateCatalogOfferCommandUseCase,
                GetProductByIdQueryUseCase,
                GetProductDetailQueryUseCase,
                SearchCatalogQueryUseCase,
                ListVariantsByProductQueryUseCase,
                ResolveVariantForCheckoutQueryUseCase,
                ResolveCurrentPriceQueryUseCase,
                GetPriceTimelineQueryUseCase,
                GetCatalogAuditQueryUseCase {

    private final CatalogProductPersistencePort productPersistencePort;
    private final CatalogVariantPersistencePort variantPersistencePort;
    private final CatalogPricePersistencePort pricePersistencePort;
    private final CatalogOfferPersistencePort offerPersistencePort;
    private final CatalogTaxonomyPersistencePort taxonomyPersistencePort;
    private final CatalogReadPersistencePort readPersistencePort;
    private final PriceSchedulePersistencePort priceSchedulePersistencePort;
    private final CatalogAuditPort catalogAuditPort;
    private final CatalogSearchCachePort catalogSearchCachePort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final DomainEventTopicPort domainEventTopicPort;
    private final ActorContextProviderPort actorContextProviderPort;
    private final ActorLegitimacyPort actorLegitimacyPort;
    private final RegionalPolicyContextPort regionalPolicyContextPort;
    private final ClockPort clockPort;
    private final CatalogResultMapper resultMapper;

    public CatalogApplicationService(
            CatalogProductPersistencePort productPersistencePort,
            CatalogVariantPersistencePort variantPersistencePort,
            CatalogPricePersistencePort pricePersistencePort,
            CatalogOfferPersistencePort offerPersistencePort,
            CatalogTaxonomyPersistencePort taxonomyPersistencePort,
            CatalogReadPersistencePort readPersistencePort,
            PriceSchedulePersistencePort priceSchedulePersistencePort,
            CatalogAuditPort catalogAuditPort,
            CatalogSearchCachePort catalogSearchCachePort,
            OutboxPersistencePort outboxPersistencePort,
            DomainEventTopicPort domainEventTopicPort,
            ActorContextProviderPort actorContextProviderPort,
            ActorLegitimacyPort actorLegitimacyPort,
            RegionalPolicyContextPort regionalPolicyContextPort,
            ClockPort clockPort,
            CatalogResultMapper resultMapper) {
        this.productPersistencePort = productPersistencePort;
        this.variantPersistencePort = variantPersistencePort;
        this.pricePersistencePort = pricePersistencePort;
        this.offerPersistencePort = offerPersistencePort;
        this.taxonomyPersistencePort = taxonomyPersistencePort;
        this.readPersistencePort = readPersistencePort;
        this.priceSchedulePersistencePort = priceSchedulePersistencePort;
        this.catalogAuditPort = catalogAuditPort;
        this.catalogSearchCachePort = catalogSearchCachePort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.domainEventTopicPort = domainEventTopicPort;
        this.actorContextProviderPort = actorContextProviderPort;
        this.actorLegitimacyPort = actorLegitimacyPort;
        this.regionalPolicyContextPort = regionalPolicyContextPort;
        this.clockPort = clockPort;
        this.resultMapper = resultMapper;
    }

    @Override
    public Mono<ProductResult> handle(CreateProductCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "PRODUCT_CREATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findProductResult(command.tenantId(), idempotency.targetId());
                    }
                    return ensureTaxonomyActive(command.tenantId(), command.brandId(), command.categoryId())
                            .then(productPersistencePort.existsByProductCode(TenantId.of(command.tenantId()), command.productCode(), null))
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new ApplicationException("product_code_no_unico", "productCode ya existe"));
                                }
                                Product product = Product.draft(
                                        TenantId.of(command.tenantId()),
                                        ProductId.newId(),
                                        command.productCode(),
                                        command.name(),
                                        command.description(),
                                        command.brandId(),
                                        command.categoryId(),
                                        now);
                                List<ProductTag> tags = toProductTags(command.tags());
                                return productPersistencePort
                                        .create(product, tags)
                                        .flatMap(created -> afterMutation(
                                                        command.tenantId(),
                                                        "PRODUCT_CREATED",
                                                        "Product",
                                                        created.productId().value(),
                                                        command.actorId(),
                                                        command.idempotencyKey(),
                                                        payloadHash,
                                                        payloadJson("productId", created.productId().value()),
                                                        new CatalogMutationEvent("Product", created.productId().value(), "ProductCreated", now))
                                                .then(productPersistencePort.findTags(TenantId.of(command.tenantId()), created.productId())
                                                        .collectList()
                                                        .map(tagsResult -> resultMapper.toProductResult(created, tagsResult))));
                            });
                });
    }

    @Override
    public Mono<ProductResult> handle(UpdateProductCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "PRODUCT_UPDATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findProductResult(command.tenantId(), idempotency.targetId());
                    }
                    return ensureTaxonomyActive(command.tenantId(), command.brandId(), command.categoryId())
                            .then(loadProduct(command.tenantId(), command.productId()))
                            .flatMap(product -> {
                                product.update(command.name(), command.description(), command.brandId(), command.categoryId(), now);
                                return productPersistencePort.update(product, toProductTags(command.tags()));
                            })
                            .flatMap(updated -> afterMutation(
                                            command.tenantId(),
                                            "PRODUCT_UPDATED",
                                            "Product",
                                            updated.productId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("productId", updated.productId().value()),
                                            new CatalogMutationEvent("Product", updated.productId().value(), "ProductUpdated", now))
                                    .thenReturn(updated))
                            .flatMap(updated -> productPersistencePort.findTags(TenantId.of(command.tenantId()), updated.productId())
                                    .collectList()
                                    .map(tags -> resultMapper.toProductResult(updated, tags)));
                });
    }

    @Override
    public Mono<ProductResult> handle(ActivateProductCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "PRODUCT_ACTIVATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findProductResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadProduct(command.tenantId(), command.productId())
                            .flatMap(product -> {
                                product.activate(now);
                                return productPersistencePort.update(product, null);
                            })
                            .flatMap(updated -> afterMutation(
                                            command.tenantId(),
                                            "PRODUCT_ACTIVATED",
                                            "Product",
                                            updated.productId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("productId", updated.productId().value()),
                                            new CatalogMutationEvent("Product", updated.productId().value(), "ProductActivated", now))
                                    .thenReturn(updated))
                            .flatMap(updated -> productPersistencePort.findTags(TenantId.of(command.tenantId()), updated.productId())
                                    .collectList()
                                    .map(tags -> resultMapper.toProductResult(updated, tags)));
                });
    }

    @Override
    public Mono<ProductResult> handle(RetireProductCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "PRODUCT_RETIRED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findProductResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadProduct(command.tenantId(), command.productId())
                            .flatMap(product -> {
                                product.retire(now);
                                return productPersistencePort.update(product, null);
                            })
                            .flatMap(updated -> afterMutation(
                                            command.tenantId(),
                                            "PRODUCT_RETIRED",
                                            "Product",
                                            updated.productId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("productId", updated.productId().value()),
                                            new CatalogMutationEvent("Product", updated.productId().value(), "ProductRetired", now))
                                    .thenReturn(updated))
                            .flatMap(updated -> productPersistencePort.findTags(TenantId.of(command.tenantId()), updated.productId())
                                    .collectList()
                                    .map(tags -> resultMapper.toProductResult(updated, tags)));
                });
    }

    @Override
    public Mono<VariantResult> handle(CreateVariantCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "VARIANT_CREATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findVariantResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadProduct(command.tenantId(), command.productId())
                            .flatMap(product -> variantPersistencePort.existsSellableSku(
                                            TenantId.of(command.tenantId()),
                                            command.sku(),
                                            null)
                                    .flatMap(existsSellableSku -> {
                                        if (existsSellableSku) {
                                            return Mono.error(new SkuNotUniqueException(command.sku()));
                                        }
                                        Variant variant = Variant.draft(
                                                TenantId.of(command.tenantId()),
                                                VariantId.newId(),
                                                product.productId(),
                                                command.sku(),
                                                command.name(),
                                                command.description(),
                                                command.weightGrams(),
                                                now);
                                        List<VariantAttribute> attributes = toVariantAttributes(command.attributes());
                                        return variantPersistencePort.create(variant, attributes)
                                                .flatMap(created -> afterMutation(
                                                                command.tenantId(),
                                                                "VARIANT_CREATED",
                                                                "Variant",
                                                                created.variantId().value(),
                                                                command.actorId(),
                                                                command.idempotencyKey(),
                                                                payloadHash,
                                                                payloadJson("variantId", created.variantId().value()),
                                                                new CatalogMutationEvent("Variant", created.variantId().value(), "VariantCreated", now))
                                                        .thenReturn(created));
                                    }))
                            .flatMap(created -> findVariantResult(command.tenantId(), created.variantId().value()));
                });
    }

    @Override
    public Mono<VariantResult> handle(UpdateVariantCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "VARIANT_UPDATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findVariantResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadVariant(command.tenantId(), command.variantId())
                            .flatMap(variant -> {
                                variant.update(command.name(), command.description(), command.weightGrams(), now);
                                return variantPersistencePort.update(variant);
                            })
                            .flatMap(updated -> afterMutation(
                                            command.tenantId(),
                                            "VARIANT_UPDATED",
                                            "Variant",
                                            updated.variantId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("variantId", updated.variantId().value()),
                                            new CatalogMutationEvent("Variant", updated.variantId().value(), "VariantUpdated", now))
                                    .thenReturn(updated))
                            .flatMap(updated -> findVariantResult(command.tenantId(), updated.variantId().value()));
                });
    }

    @Override
    public Mono<VariantResult> handle(ChangeVariantStatusCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        String actionType = "VARIANT_STATUS_CHANGED";
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), actionType, command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findVariantResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadVariant(command.tenantId(), command.variantId())
                            .flatMap(variant -> loadProduct(command.tenantId(), variant.productId().value())
                                    .flatMap(product -> {
                                        if ("SELLABLE".equalsIgnoreCase(command.targetStatus())) {
                                            return variantPersistencePort
                                                    .findAttributes(TenantId.of(command.tenantId()), variant.variantId())
                                                    .collectList()
                                                    .flatMap(attributes -> variantPersistencePort.existsSellableSku(
                                                                    TenantId.of(command.tenantId()),
                                                                    variant.sku(),
                                                                    variant.variantId().value())
                                                            .flatMap(exists -> {
                                                                if (exists) {
                                                                    return Mono.error(new SkuNotUniqueException(variant.sku()));
                                                                }
                                                                variant.markSellable(
                                                                        product.status(),
                                                                        CatalogOfferPolicy.hasRequiredAttributes(attributes),
                                                                        command.sellableFrom(),
                                                                        command.sellableUntil(),
                                                                        now);
                                                                return variantPersistencePort.update(variant);
                                                            }));
                                        }
                                        if ("DISCONTINUED".equalsIgnoreCase(command.targetStatus())) {
                                            variant.discontinue(now);
                                            return variantPersistencePort.update(variant);
                                        }
                                        return Mono.error(new ApplicationException("estado_variante_invalido", "targetStatus no soportado"));
                                    }))
                            .flatMap(updated -> afterMutation(
                                            command.tenantId(),
                                            actionType,
                                            "Variant",
                                            updated.variantId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("variantId", updated.variantId().value()),
                                            new CatalogMutationEvent(
                                                    "Variant",
                                                    updated.variantId().value(),
                                                    "SELLABLE".equalsIgnoreCase(command.targetStatus())
                                                            ? "VariantSellabilityChanged"
                                                            : "VariantDiscontinued",
                                                    now))
                                    .thenReturn(updated))
                            .flatMap(updated -> findVariantResult(command.tenantId(), updated.variantId().value()));
                });
    }

    @Override
    public Mono<VariantResult> handle(UpsertVariantAttributesCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "VARIANT_ATTRIBUTES_UPSERT", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findVariantResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadVariant(command.tenantId(), command.variantId())
                            .flatMap(variant -> variantPersistencePort
                                    .replaceAttributes(
                                            TenantId.of(command.tenantId()),
                                            variant.variantId(),
                                            toVariantAttributes(command.attributes()))
                                    .thenReturn(variant))
                            .flatMap(variant -> afterMutation(
                                            command.tenantId(),
                                            "VARIANT_ATTRIBUTES_UPSERT",
                                            "Variant",
                                            variant.variantId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("variantId", variant.variantId().value()),
                                            new CatalogMutationEvent("Variant", variant.variantId().value(), "VariantAttributesUpserted", now))
                                    .thenReturn(variant))
                            .flatMap(variant -> findVariantResult(command.tenantId(), variant.variantId().value()));
                });
    }

    @Override
    public Mono<PriceResult> handle(RegisterPriceCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "PRICE_CREATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findPriceResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadVariant(command.tenantId(), command.variantId())
                            .flatMap(variant -> {
                                Price price = Price.register(
                                        TenantId.of(command.tenantId()),
                                        PriceId.newId(),
                                        variant.variantId(),
                                        parsePriceType(command.priceType()),
                                        Money.of(command.amount(), command.currency()),
                                        TimeWindow.of(command.effectiveFrom(), command.effectiveUntil()),
                                        now);
                                return pricePersistencePort
                                        .findTimeline(
                                                TenantId.of(command.tenantId()),
                                                variant.variantId(),
                                                command.currency(),
                                                parsePriceType(command.priceType()))
                                        .collectList()
                                        .flatMap(existing -> {
                                            Price.ensureNoOverlap(price, existing);
                                            return pricePersistencePort.create(price);
                                        });
                            })
                            .flatMap(created -> afterMutation(
                                            command.tenantId(),
                                            "PRICE_CREATED",
                                            "Price",
                                            created.priceId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("priceId", created.priceId().value()),
                                            new CatalogMutationEvent("Price", created.priceId().value(), "PriceCreated", now))
                                    .thenReturn(created))
                            .map(resultMapper::toPriceResult);
                });
    }

    @Override
    public Mono<PriceResult> handle(UpdatePriceCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "PRICE_UPDATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findPriceResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadPrice(command.tenantId(), command.priceId())
                            .flatMap(price -> pricePersistencePort
                                    .findTimeline(
                                            TenantId.of(command.tenantId()),
                                            price.variantId(),
                                            command.currency(),
                                            parsePriceType(command.priceType()))
                                    .collectList()
                                    .flatMap(existing -> {
                                        price.update(
                                                Money.of(command.amount(), command.currency()),
                                                TimeWindow.of(command.effectiveFrom(), command.effectiveUntil()),
                                                now);
                                        Price.ensureNoOverlap(price, existing);
                                        return pricePersistencePort.update(price);
                                    }))
                            .flatMap(updated -> afterMutation(
                                            command.tenantId(),
                                            "PRICE_UPDATED",
                                            "Price",
                                            updated.priceId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("priceId", updated.priceId().value()),
                                            new CatalogMutationEvent("Price", updated.priceId().value(), "PriceUpdated", now))
                                    .thenReturn(updated))
                            .map(resultMapper::toPriceResult);
                });
    }

    @Override
    public Mono<Void> handle(SchedulePriceActivationCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "PRICE_SCHEDULED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return Mono.empty();
                    }
                    return loadPrice(command.tenantId(), command.priceId())
                            .flatMap(price -> priceSchedulePersistencePort
                                    .create(new PriceSchedule(
                                            UUID.randomUUID().toString(),
                                            command.tenantId(),
                                            price.priceId().value(),
                                            command.executeAfter(),
                                            PriceScheduleJobStatus.PENDING,
                                            null,
                                            now,
                                            now)))
                            .flatMap(schedule -> afterMutation(
                                    command.tenantId(),
                                    "PRICE_SCHEDULED",
                                    "Price",
                                    command.priceId(),
                                    command.actorId(),
                                    command.idempotencyKey(),
                                    payloadHash,
                                    payloadJson("priceId", command.priceId()),
                                    new CatalogMutationEvent("Price", command.priceId(), "PriceScheduled", now)));
                });
    }

    @Override
    public Mono<CatalogOfferResult> handle(PublishCatalogOfferCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "CATALOG_OFFER_PUBLISHED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findOfferResult(command.tenantId(), idempotency.targetId());
                    }
                    return composeOffer(
                                    command.tenantId(),
                                    command.productId(),
                                    command.variantId(),
                                    command.priceId(),
                                    command.regionalPolicyReference())
                            .flatMap(offerPersistencePort::create)
                            .flatMap(offer -> storeDomainEvents(offer.pullDomainEvents()).thenReturn(offer))
                            .flatMap(offer -> afterMutation(
                                            command.tenantId(),
                                            "CATALOG_OFFER_PUBLISHED",
                                            "CatalogOffer",
                                            offer.offerId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("offerId", offer.offerId().value()),
                                            null)
                                    .thenReturn(offer))
                            .map(resultMapper::toOfferResult);
                });
    }

    @Override
    public Mono<CatalogOfferResult> handle(UpdateCatalogOfferCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true)
                .then(checkIdempotency(command.tenantId(), "CATALOG_OFFER_UPDATED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findOfferResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadOffer(command.tenantId(), command.offerId())
                            .flatMap(existing -> composeOffer(
                                            command.tenantId(),
                                            existing.product().productId().value(),
                                            command.variantId(),
                                            command.priceId(),
                                            command.regionalPolicyReference())
                                    .map(composed -> {
                                        existing.updateOffer(
                                                composed.variant(),
                                                composed.price(),
                                                composed.regionalPolicyReference(),
                                                now);
                                        return existing;
                                    }))
                            .flatMap(offerPersistencePort::update)
                            .flatMap(offer -> storeDomainEvents(offer.pullDomainEvents()).thenReturn(offer))
                            .flatMap(offer -> afterMutation(
                                            command.tenantId(),
                                            "CATALOG_OFFER_UPDATED",
                                            "CatalogOffer",
                                            offer.offerId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson("offerId", offer.offerId().value()),
                                            null)
                                    .thenReturn(offer))
                            .map(resultMapper::toOfferResult);
                });
    }

    @Override
    public Mono<ProductResult> handle(GetProductByIdQuery query) {
        return requireActor(query.tenantId(), false).then(findProductResult(query.tenantId(), query.productId()));
    }

    @Override
    public Mono<ProductDetailResult> handle(GetProductDetailQuery query) {
        return requireActor(query.tenantId(), false)
                .then(findProductResult(query.tenantId(), query.productId()))
                .zipWith(variantPersistencePort
                        .findByProductId(TenantId.of(query.tenantId()), ProductId.of(query.productId()))
                        .flatMap(variant -> findVariantResult(query.tenantId(), variant.variantId().value()))
                        .collectList())
                .flatMap(tuple -> {
                    ProductResult product = tuple.getT1();
                    List<VariantResult> variants = tuple.getT2();
                    return Flux.fromIterable(variants)
                            .flatMap(variant -> pricePersistencePort
                                    .resolveActive(
                                            TenantId.of(query.tenantId()),
                                            VariantId.of(variant.variantId()),
                                            "COP",
                                            PriceType.BASE,
                                            query.at() == null ? clockPort.now() : query.at())
                                    .map(resultMapper::toPriceResult)
                                    .onErrorResume(error -> Mono.empty()))
                            .collectList()
                            .map(activePrices -> new ProductDetailResult(product, variants, activePrices));
                });
    }

    @Override
    public Mono<CatalogSearchResult> handle(SearchCatalogQuery query) {
        return requireActor(query.tenantId(), false)
                .then(Mono.defer(() -> {
                    String cacheKey = cacheKeyFor(query);
                    return catalogSearchCachePort.get(cacheKey)
                            .switchIfEmpty(readPersistencePort
                                    .search(toFilter(query))
                                    .map(resultMapper::toSearchItemResult)
                                    .collectList()
                                    .zipWith(readPersistencePort.count(toFilter(query)))
                                    .map(tuple -> new CatalogSearchResult(
                                            tuple.getT1(),
                                            query.page(),
                                            query.size(),
                                            tuple.getT2()))
                                    .flatMap(result -> catalogSearchCachePort.put(cacheKey, result).thenReturn(result)));
                }));
    }

    @Override
    public Flux<VariantResult> handle(ListVariantsByProductQuery query) {
        return requireActor(query.tenantId(), false)
                .thenMany(variantPersistencePort
                        .findByProductId(TenantId.of(query.tenantId()), ProductId.of(query.productId()))
                        .flatMap(variant -> findVariantResult(query.tenantId(), variant.variantId().value())));
    }

    @Override
    public Mono<CheckoutVariantResolutionResult> handle(ResolveVariantForCheckoutQuery query) {
        return requireActor(query.tenantId(), false)
                .then(variantPersistencePort.findBySku(TenantId.of(query.tenantId()), query.sku())
                        .switchIfEmpty(Mono.error(new CatalogResourceNotFoundException("Variant", query.sku()))))
                .flatMap(variant -> {
                    Instant at = query.at() == null ? clockPort.now() : query.at();
                    if (!variant.isSellableAt(at)) {
                        return Mono.error(new VariantNotSellableException());
                    }
                    PriceType priceType = parsePriceType(query.priceType());
                    return pricePersistencePort.resolveActive(
                                    TenantId.of(query.tenantId()),
                                    variant.variantId(),
                                    query.currency(),
                                    priceType,
                                    at)
                            .switchIfEmpty(Mono.error(new VariantNotSellableException()))
                            .map(price -> new CheckoutVariantResolutionResult(
                                    query.tenantId(),
                                    variant.productId().value(),
                                    variant.variantId().value(),
                                    variant.sku(),
                                    price.priceId().value(),
                                    price.money().amount(),
                                    price.money().currency(),
                                    price.priceType().name(),
                                    at));
                });
    }

    @Override
    public Mono<PriceResult> handle(ResolveCurrentPriceQuery query) {
        return requireActor(query.tenantId(), false)
                .then(pricePersistencePort.resolveActive(
                        TenantId.of(query.tenantId()),
                        VariantId.of(query.variantId()),
                        query.currency(),
                        parsePriceType(query.priceType()),
                        query.at() == null ? clockPort.now() : query.at()))
                .switchIfEmpty(Mono.error(new CatalogResourceNotFoundException("Price", query.variantId())))
                .map(resultMapper::toPriceResult);
    }

    @Override
    public Mono<PriceTimelineResult> handle(GetPriceTimelineQuery query) {
        return requireActor(query.tenantId(), false)
                .then(pricePersistencePort
                        .findTimeline(
                                TenantId.of(query.tenantId()),
                                VariantId.of(query.variantId()),
                                query.currency(),
                                parsePriceType(query.priceType()))
                        .map(resultMapper::toPriceResult)
                        .collectList()
                        .map(timeline -> new PriceTimelineResult(
                                query.variantId(),
                                query.currency(),
                                query.priceType(),
                                timeline)));
    }

    @Override
    public Mono<CatalogAuditResult> handle(GetCatalogAuditQuery query) {
        return requireActor(query.tenantId(), true)
                .then(catalogAuditPort
                        .findByTarget(query.tenantId(), query.targetType(), query.targetId(), query.page() * query.size(), query.size())
                        .map(resultMapper::toAuditEntryResult)
                        .collectList()
                        .zipWith(catalogAuditPort.countByTarget(query.tenantId(), query.targetType(), query.targetId()))
                        .map(tuple -> new CatalogAuditResult(tuple.getT1(), query.page(), query.size(), tuple.getT2())));
    }

    private Mono<Void> ensureTaxonomyActive(String tenantId, String brandId, String categoryId) {
        return taxonomyPersistencePort.isBrandActive(tenantId, brandId)
                .zipWith(taxonomyPersistencePort.isCategoryActive(tenantId, categoryId))
                .flatMap(result -> {
                    if (!result.getT1() || !result.getT2()) {
                        return Mono.error(new BrandOrCategoryInvalidException());
                    }
                    return Mono.empty();
                });
    }

    private Mono<Product> loadProduct(String tenantId, String productId) {
        return productPersistencePort
                .findById(TenantId.of(tenantId), ProductId.of(productId))
                .switchIfEmpty(Mono.error(new CatalogResourceNotFoundException("Product", productId)));
    }

    private Mono<Variant> loadVariant(String tenantId, String variantId) {
        return variantPersistencePort
                .findById(TenantId.of(tenantId), VariantId.of(variantId))
                .switchIfEmpty(Mono.error(new CatalogResourceNotFoundException("Variant", variantId)));
    }

    private Mono<Price> loadPrice(String tenantId, String priceId) {
        return pricePersistencePort
                .findById(TenantId.of(tenantId), PriceId.of(priceId))
                .switchIfEmpty(Mono.error(new CatalogResourceNotFoundException("Price", priceId)));
    }

    private Mono<CatalogOffer> loadOffer(String tenantId, String offerId) {
        return offerPersistencePort
                .findByOfferId(TenantId.of(tenantId), OfferId.of(offerId))
                .switchIfEmpty(Mono.error(new CatalogResourceNotFoundException("CatalogOffer", offerId)));
    }

    private Mono<ProductResult> findProductResult(String tenantId, String productId) {
        return loadProduct(tenantId, productId)
                .flatMap(product -> productPersistencePort
                        .findTags(TenantId.of(tenantId), product.productId())
                        .collectList()
                        .map(tags -> resultMapper.toProductResult(product, tags)));
    }

    private Mono<VariantResult> findVariantResult(String tenantId, String variantId) {
        return loadVariant(tenantId, variantId)
                .flatMap(variant -> variantPersistencePort
                        .findAttributes(TenantId.of(tenantId), variant.variantId())
                        .collectList()
                        .map(attrs -> resultMapper.toVariantResult(variant, attrs)));
    }

    private Mono<PriceResult> findPriceResult(String tenantId, String priceId) {
        return loadPrice(tenantId, priceId).map(resultMapper::toPriceResult);
    }

    private Mono<CatalogOfferResult> findOfferResult(String tenantId, String offerId) {
        return loadOffer(tenantId, offerId).map(resultMapper::toOfferResult);
    }

    private Mono<CatalogOffer> composeOffer(
            String tenantId,
            String productId,
            String variantId,
            String priceId,
            String regionalPolicyReference) {
        Instant now = clockPort.now();
        return loadProduct(tenantId, productId)
                .zipWith(loadVariant(tenantId, variantId))
                .zipWith(loadPrice(tenantId, priceId))
                .flatMap(tuple -> {
                    Product product = tuple.getT1().getT1();
                    Variant variant = tuple.getT1().getT2();
                    Price price = tuple.getT2();
                    if (regionalPolicyReference != null && !regionalPolicyReference.isBlank()) {
                        return Mono.just(CatalogOffer.publish(product, variant, price, regionalPolicyReference, now));
                    }
                    return regionalPolicyContextPort
                            .resolveForTenant(tenantId, "")
                            .defaultIfEmpty(new RegionalPolicyContext("", "", ""))
                            .map(policy -> CatalogOffer.publish(product, variant, price, policy.policyReference(), now));
                });
    }

    private Mono<Void> storeDomainEvents(List<DomainEvent> events) {
        return Flux.fromIterable(events)
                .concatMap(event -> outboxPersistencePort.store(event, domainEventPayload(event)))
                .then();
    }

    private List<ProductTag> toProductTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        List<ProductTag> normalized = new ArrayList<>();
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            normalized.add(new ProductTag("tag", tag.trim()));
        }
        return normalized;
    }

    private List<VariantAttribute> toVariantAttributes(List<VariantAttributeInput> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return List.of();
        }
        List<VariantAttribute> normalized = new ArrayList<>();
        for (VariantAttributeInput input : attributes) {
            normalized.add(new VariantAttribute(input.attributeCode(), input.value(), input.normalizedValue()));
        }
        return normalized;
    }

    private Mono<ActorContext> requireActor(String tenantId, boolean adminRequired) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.error(new ApplicationException("tenant_requerido", "tenantId es obligatorio"));
        }
        return actorContextProviderPort.currentActor()
                .switchIfEmpty(Mono.error(new OperationNotPermittedException(
                        "actor_no_autenticado",
                        "No hay actor autenticado disponible para ejecutar la operacion")))
                .flatMap(actor -> {
                    if (adminRequired && !actor.admin() && !actor.trustedService()) {
                        return Mono.error(new OperationNotPermittedException(
                                "operacion_no_permitida",
                                "La operacion requiere rol administrativo"));
                    }
                    if (!actor.admin() && !tenantId.equals(actor.tenantId())) {
                        return Mono.error(new CrossTenantAccessException());
                    }
                    return actorLegitimacyPort
                            .isLegitimate(actor.actorId(), tenantId)
                            .flatMap(valid -> valid
                                    ? Mono.just(actor)
                                    : Mono.error(new ActorNotLegitimateException()));
                });
    }

    private Mono<IdempotencyDecision> checkIdempotency(
            String tenantId,
            String actionType,
            String idempotencyKey,
            String payloadHash) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.just(IdempotencyDecision.none());
        }
        return catalogAuditPort
                .findByIdempotency(tenantId, actionType, idempotencyKey)
                .flatMap(existing -> {
                    if (!payloadHash.equals(existing.payloadHash())) {
                        return Mono.error(new IdempotencyConflictException());
                    }
                    return Mono.just(new IdempotencyDecision(true, existing.targetId()));
                })
                .switchIfEmpty(Mono.just(IdempotencyDecision.none()));
    }

    private Mono<Void> afterMutation(
            String tenantId,
            String actionType,
            String targetType,
            String targetId,
            String actorId,
            String idempotencyKey,
            String payloadHash,
            String payload,
            DomainEvent optionalEvent) {
        Instant now = clockPort.now();
        CatalogAuditEntry audit = new CatalogAuditEntry(
                UUID.randomUUID().toString(),
                tenantId,
                actorId,
                actionType,
                targetType,
                targetId,
                "SUCCESS",
                payload,
                idempotencyKey,
                payloadHash,
                now);
        Mono<Void> storeAudit = catalogAuditPort.record(audit);
        Mono<Void> storeEvent = optionalEvent == null
                ? Mono.empty()
                : outboxPersistencePort.store(optionalEvent, payloadWithEventType(payload, optionalEvent.eventType()));
        return storeAudit
                .then(storeEvent)
                .then(catalogSearchCachePort.evictTenant(tenantId));
    }

    private String payloadWithEventType(String payload, String eventType) {
        return "{\"eventType\":\"" + eventType + "\",\"payload\":" + payload + "}";
    }

    private String domainEventPayload(DomainEvent event) {
        String topic = domainEventTopicPort.topicFor(event.eventType());
        return "{"
                + "\"eventId\":\"" + event.eventId() + "\"," 
                + "\"eventType\":\"" + event.eventType() + "\"," 
                + "\"topic\":\"" + topic + "\"," 
                + "\"aggregateType\":\"" + event.aggregateType() + "\"," 
                + "\"aggregateId\":\"" + event.aggregateId() + "\"," 
                + "\"occurredAt\":\"" + event.occurredAt() + "\""
                + "}";
    }

    private String payloadJson(String key, String value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    private CatalogSearchFilter toFilter(SearchCatalogQuery query) {
        int size = query.size() <= 0 ? 20 : Math.min(query.size(), 100);
        int page = Math.max(query.page(), 0);
        return new CatalogSearchFilter(
                query.tenantId(),
                query.text(),
                query.brandId(),
                query.categoryId(),
                query.variantStatus(),
                page * size,
                size,
                query.at());
    }

    private String cacheKeyFor(SearchCatalogQuery query) {
        return String.join(
                "::",
                query.tenantId(),
                String.valueOf(query.page()),
                String.valueOf(query.size()),
                nullSafe(query.text()),
                nullSafe(query.brandId()),
                nullSafe(query.categoryId()),
                nullSafe(query.variantStatus()));
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private PriceType parsePriceType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return PriceType.BASE;
        }
        try {
            return PriceType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApplicationException("precio_invalido", "priceType invalido: " + rawType);
        }
    }

    private record IdempotencyDecision(boolean replayed, String targetId) {

        private static IdempotencyDecision none() {
            return new IdempotencyDecision(false, null);
        }
    }
}

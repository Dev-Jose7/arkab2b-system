package com.arka.catalog.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.catalog.application.command.CreateVariantCommand;
import com.arka.catalog.application.command.RegisterPriceCommand;
import com.arka.catalog.application.command.VariantAttributeInput;
import com.arka.catalog.application.mapper.result.CatalogResultMapper;
import com.arka.catalog.application.port.out.audit.CatalogAuditPort;
import com.arka.catalog.application.port.out.cache.CatalogSearchCachePort;
import com.arka.catalog.application.port.out.directory.RegionalPolicyContextPort;
import com.arka.catalog.application.port.out.event.DomainEventTopicPort;
import com.arka.catalog.application.port.out.external.ActorLegitimacyPort;
import com.arka.catalog.application.port.out.external.ClockPort;
import com.arka.catalog.application.port.out.persistence.CatalogOfferPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogPricePersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogProductPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogReadPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogTaxonomyPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogVariantPersistencePort;
import com.arka.catalog.application.port.out.persistence.OutboxPersistencePort;
import com.arka.catalog.application.port.out.persistence.PriceSchedulePersistencePort;
import com.arka.catalog.application.port.out.security.ActorContext;
import com.arka.catalog.application.port.out.security.ActorContextProviderPort;
import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.exception.PricePeriodOverlapException;
import com.arka.catalog.domain.catalogoffer.exception.SkuNotUniqueException;
import com.arka.catalog.domain.catalogoffer.valueobject.Money;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.TimeWindow;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import com.arka.catalog.domain.shared.exception.OperationNotPermittedException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CatalogApplicationServiceTest {

    @Mock
    private CatalogProductPersistencePort productPersistencePort;

    @Mock
    private CatalogVariantPersistencePort variantPersistencePort;

    @Mock
    private CatalogPricePersistencePort pricePersistencePort;

    @Mock
    private CatalogOfferPersistencePort offerPersistencePort;

    @Mock
    private CatalogTaxonomyPersistencePort taxonomyPersistencePort;

    @Mock
    private CatalogReadPersistencePort readPersistencePort;

    @Mock
    private PriceSchedulePersistencePort priceSchedulePersistencePort;

    @Mock
    private CatalogAuditPort catalogAuditPort;

    @Mock
    private CatalogSearchCachePort catalogSearchCachePort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private DomainEventTopicPort domainEventTopicPort;

    @Mock
    private ActorContextProviderPort actorContextProviderPort;

    @Mock
    private ActorLegitimacyPort actorLegitimacyPort;

    @Mock
    private RegionalPolicyContextPort regionalPolicyContextPort;

    @Mock
    private ClockPort clockPort;

    private CatalogApplicationService service;

    @BeforeEach
    void setUp() {
        service = new CatalogApplicationService(
                productPersistencePort,
                variantPersistencePort,
                pricePersistencePort,
                offerPersistencePort,
                taxonomyPersistencePort,
                readPersistencePort,
                priceSchedulePersistencePort,
                catalogAuditPort,
                catalogSearchCachePort,
                outboxPersistencePort,
                domainEventTopicPort,
                actorContextProviderPort,
                actorLegitimacyPort,
                regionalPolicyContextPort,
                clockPort,
                new CatalogResultMapper());
    }

    @Test
    void shouldRejectCreateVariantWhenSellableSkuAlreadyExists() {
        Instant now = Instant.parse("2026-04-01T10:00:00Z");
        CreateVariantCommand command = new CreateVariantCommand(
                "tenant-demo",
                "actor-1",
                "product-1",
                "SKU-100",
                "Variant 100",
                "desc",
                100,
                List.of(new VariantAttributeInput("color", "red", "red")),
                "idem-100");

        Product product = Product.draft(
                TenantId.of("tenant-demo"),
                ProductId.of("product-1"),
                "PROD-1",
                "Product",
                "desc",
                "brand-acme",
                "category-beverages",
                now);

        when(clockPort.now()).thenReturn(now);
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("actor-1", "tenant-demo", "CO", true, false)));
        when(actorLegitimacyPort.isLegitimate("actor-1", "tenant-demo")).thenReturn(Mono.just(Boolean.TRUE));
        when(catalogAuditPort.findByIdempotency("tenant-demo", "VARIANT_CREATED", "idem-100"))
                .thenReturn(Mono.empty());
        when(productPersistencePort.findById(any(), any())).thenReturn(Mono.just(product));
        when(variantPersistencePort.existsSellableSku(any(), eq("SKU-100"), eq(null))).thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(service.handle(command))
                .expectError(SkuNotUniqueException.class)
                .verify();

        verify(variantPersistencePort, never()).create(any(), any());
    }

    @Test
    void shouldRejectRegisterPriceWhenItOverlapsTimeline() {
        Instant now = Instant.parse("2026-04-02T10:00:00Z");
        RegisterPriceCommand command = new RegisterPriceCommand(
                "tenant-demo",
                "actor-1",
                "variant-1",
                new BigDecimal("15.00"),
                "COP",
                "BASE",
                now.plusSeconds(300),
                now.plusSeconds(3600),
                "idem-price-1");

        Variant variant = Variant.draft(
                TenantId.of("tenant-demo"),
                VariantId.of("variant-1"),
                ProductId.of("product-1"),
                "SKU-1",
                "Variant",
                "desc",
                100,
                now);

        Price existing = Price.register(
                TenantId.of("tenant-demo"),
                PriceId.of("price-existing"),
                variant.variantId(),
                PriceType.BASE,
                Money.of(new BigDecimal("13.00"), "COP"),
                TimeWindow.of(now, now.plusSeconds(1200)),
                now);

        when(clockPort.now()).thenReturn(now);
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("actor-1", "tenant-demo", "CO", true, false)));
        when(actorLegitimacyPort.isLegitimate("actor-1", "tenant-demo")).thenReturn(Mono.just(Boolean.TRUE));
        when(catalogAuditPort.findByIdempotency("tenant-demo", "PRICE_CREATED", "idem-price-1"))
                .thenReturn(Mono.empty());
        when(variantPersistencePort.findById(any(), any())).thenReturn(Mono.just(variant));
        when(pricePersistencePort.findTimeline(any(), any(), eq("COP"), eq(PriceType.BASE))).thenReturn(Flux.just(existing));

        StepVerifier.create(service.handle(command))
                .expectError(PricePeriodOverlapException.class)
                .verify();

        verify(pricePersistencePort, never()).create(any());
    }

    @Test
    void shouldRejectMutationWhenActorContextIsMissing() {
        Instant now = Instant.parse("2026-04-01T10:00:00Z");
        CreateVariantCommand command = new CreateVariantCommand(
                "tenant-demo",
                "actor-1",
                "product-1",
                "SKU-200",
                "Variant 200",
                "desc",
                100,
                List.of(new VariantAttributeInput("size", "m", "m")),
                "idem-200");

        when(clockPort.now()).thenReturn(now);
        when(actorContextProviderPort.currentActor()).thenReturn(Mono.empty());
        when(catalogAuditPort.findByIdempotency(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(command))
                .expectError(OperationNotPermittedException.class)
                .verify();
    }
}

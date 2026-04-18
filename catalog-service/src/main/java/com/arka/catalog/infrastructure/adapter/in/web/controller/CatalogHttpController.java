package com.arka.catalog.infrastructure.adapter.in.web.controller;

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
import com.arka.catalog.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.catalog.infrastructure.adapter.in.web.mapper.command.CatalogCommandMapper;
import com.arka.catalog.infrastructure.adapter.in.web.mapper.query.CatalogQueryMapper;
import com.arka.catalog.infrastructure.adapter.in.web.mapper.response.CatalogResponseMapper;
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
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogAuditResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogOfferResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CatalogSearchResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.CheckoutVariantResolutionResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.PriceResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.PriceTimelineResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.ProductDetailResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.ProductResponse;
import com.arka.catalog.infrastructure.adapter.in.web.response.VariantResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/catalog")
public class CatalogHttpController {

    private final CatalogCommandMapper commandMapper;
    private final CatalogQueryMapper queryMapper;
    private final CatalogResponseMapper responseMapper;
    private final CreateProductCommandUseCase createProductCommandUseCase;
    private final UpdateProductCommandUseCase updateProductCommandUseCase;
    private final ActivateProductCommandUseCase activateProductCommandUseCase;
    private final RetireProductCommandUseCase retireProductCommandUseCase;
    private final CreateVariantCommandUseCase createVariantCommandUseCase;
    private final UpdateVariantCommandUseCase updateVariantCommandUseCase;
    private final ChangeVariantStatusCommandUseCase changeVariantStatusCommandUseCase;
    private final UpsertVariantAttributesCommandUseCase upsertVariantAttributesCommandUseCase;
    private final RegisterPriceCommandUseCase registerPriceCommandUseCase;
    private final UpdatePriceCommandUseCase updatePriceCommandUseCase;
    private final SchedulePriceActivationCommandUseCase schedulePriceActivationCommandUseCase;
    private final PublishCatalogOfferCommandUseCase publishCatalogOfferCommandUseCase;
    private final UpdateCatalogOfferCommandUseCase updateCatalogOfferCommandUseCase;
    private final GetProductByIdQueryUseCase getProductByIdQueryUseCase;
    private final GetProductDetailQueryUseCase getProductDetailQueryUseCase;
    private final SearchCatalogQueryUseCase searchCatalogQueryUseCase;
    private final ListVariantsByProductQueryUseCase listVariantsByProductQueryUseCase;
    private final ResolveVariantForCheckoutQueryUseCase resolveVariantForCheckoutQueryUseCase;
    private final ResolveCurrentPriceQueryUseCase resolveCurrentPriceQueryUseCase;
    private final GetPriceTimelineQueryUseCase getPriceTimelineQueryUseCase;
    private final GetCatalogAuditQueryUseCase getCatalogAuditQueryUseCase;

    public CatalogHttpController(
            CatalogCommandMapper commandMapper,
            CatalogQueryMapper queryMapper,
            CatalogResponseMapper responseMapper,
            CreateProductCommandUseCase createProductCommandUseCase,
            UpdateProductCommandUseCase updateProductCommandUseCase,
            ActivateProductCommandUseCase activateProductCommandUseCase,
            RetireProductCommandUseCase retireProductCommandUseCase,
            CreateVariantCommandUseCase createVariantCommandUseCase,
            UpdateVariantCommandUseCase updateVariantCommandUseCase,
            ChangeVariantStatusCommandUseCase changeVariantStatusCommandUseCase,
            UpsertVariantAttributesCommandUseCase upsertVariantAttributesCommandUseCase,
            RegisterPriceCommandUseCase registerPriceCommandUseCase,
            UpdatePriceCommandUseCase updatePriceCommandUseCase,
            SchedulePriceActivationCommandUseCase schedulePriceActivationCommandUseCase,
            PublishCatalogOfferCommandUseCase publishCatalogOfferCommandUseCase,
            UpdateCatalogOfferCommandUseCase updateCatalogOfferCommandUseCase,
            GetProductByIdQueryUseCase getProductByIdQueryUseCase,
            GetProductDetailQueryUseCase getProductDetailQueryUseCase,
            SearchCatalogQueryUseCase searchCatalogQueryUseCase,
            ListVariantsByProductQueryUseCase listVariantsByProductQueryUseCase,
            ResolveVariantForCheckoutQueryUseCase resolveVariantForCheckoutQueryUseCase,
            ResolveCurrentPriceQueryUseCase resolveCurrentPriceQueryUseCase,
            GetPriceTimelineQueryUseCase getPriceTimelineQueryUseCase,
            GetCatalogAuditQueryUseCase getCatalogAuditQueryUseCase) {
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.responseMapper = responseMapper;
        this.createProductCommandUseCase = createProductCommandUseCase;
        this.updateProductCommandUseCase = updateProductCommandUseCase;
        this.activateProductCommandUseCase = activateProductCommandUseCase;
        this.retireProductCommandUseCase = retireProductCommandUseCase;
        this.createVariantCommandUseCase = createVariantCommandUseCase;
        this.updateVariantCommandUseCase = updateVariantCommandUseCase;
        this.changeVariantStatusCommandUseCase = changeVariantStatusCommandUseCase;
        this.upsertVariantAttributesCommandUseCase = upsertVariantAttributesCommandUseCase;
        this.registerPriceCommandUseCase = registerPriceCommandUseCase;
        this.updatePriceCommandUseCase = updatePriceCommandUseCase;
        this.schedulePriceActivationCommandUseCase = schedulePriceActivationCommandUseCase;
        this.publishCatalogOfferCommandUseCase = publishCatalogOfferCommandUseCase;
        this.updateCatalogOfferCommandUseCase = updateCatalogOfferCommandUseCase;
        this.getProductByIdQueryUseCase = getProductByIdQueryUseCase;
        this.getProductDetailQueryUseCase = getProductDetailQueryUseCase;
        this.searchCatalogQueryUseCase = searchCatalogQueryUseCase;
        this.listVariantsByProductQueryUseCase = listVariantsByProductQueryUseCase;
        this.resolveVariantForCheckoutQueryUseCase = resolveVariantForCheckoutQueryUseCase;
        this.resolveCurrentPriceQueryUseCase = resolveCurrentPriceQueryUseCase;
        this.getPriceTimelineQueryUseCase = getPriceTimelineQueryUseCase;
        this.getCatalogAuditQueryUseCase = getCatalogAuditQueryUseCase;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/products")
    public Mono<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return createProductCommandUseCase
                .handle(commandMapper.toCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PutMapping("/products/{productId}")
    public Mono<ProductResponse> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateProductCommandUseCase
                .handle(commandMapper.toCommand(productId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/products/{productId}/activate")
    public Mono<ProductResponse> activateProduct(
            @PathVariable String productId,
            @RequestParam(name = "idempotencyKey", required = false) String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return activateProductCommandUseCase
                .handle(commandMapper.toActivateCommand(productId, idempotencyKey, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/products/{productId}/retire")
    public Mono<ProductResponse> retireProduct(
            @PathVariable String productId,
            @RequestParam(name = "idempotencyKey", required = false) String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return retireProductCommandUseCase
                .handle(commandMapper.toRetireCommand(productId, idempotencyKey, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/products/{productId}/variants")
    public Mono<VariantResponse> createVariant(
            @PathVariable String productId,
            @Valid @RequestBody CreateVariantRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return createVariantCommandUseCase
                .handle(commandMapper.toCommand(productId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PutMapping("/variants/{variantId}")
    public Mono<VariantResponse> updateVariant(
            @PathVariable String variantId,
            @Valid @RequestBody UpdateVariantRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateVariantCommandUseCase
                .handle(commandMapper.toCommand(variantId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/variants/{variantId}/status")
    public Mono<VariantResponse> changeVariantStatus(
            @PathVariable String variantId,
            @Valid @RequestBody ChangeVariantStatusRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return changeVariantStatusCommandUseCase
                .handle(commandMapper.toCommand(variantId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PutMapping("/variants/{variantId}/attributes")
    public Mono<VariantResponse> upsertVariantAttributes(
            @PathVariable String variantId,
            @Valid @RequestBody UpsertVariantAttributesRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return upsertVariantAttributesCommandUseCase
                .handle(commandMapper.toCommand(variantId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/variants/{variantId}/prices")
    public Mono<PriceResponse> registerPrice(
            @PathVariable String variantId,
            @Valid @RequestBody RegisterPriceRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return registerPriceCommandUseCase
                .handle(commandMapper.toCommand(variantId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PutMapping("/prices/{priceId}")
    public Mono<PriceResponse> updatePrice(
            @PathVariable String priceId,
            @Valid @RequestBody UpdatePriceRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updatePriceCommandUseCase
                .handle(commandMapper.toCommand(priceId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/prices/{priceId}/schedules/activation")
    public Mono<Void> schedulePriceActivation(
            @PathVariable String priceId,
            @Valid @RequestBody SchedulePriceActivationRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return schedulePriceActivationCommandUseCase.handle(commandMapper.toCommand(priceId, request, principal));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PostMapping("/offers/publish")
    public Mono<CatalogOfferResponse> publishOffer(
            @Valid @RequestBody PublishCatalogOfferRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return publishCatalogOfferCommandUseCase
                .handle(commandMapper.toCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @PutMapping("/offers/{offerId}")
    public Mono<CatalogOfferResponse> updateOffer(
            @PathVariable String offerId,
            @Valid @RequestBody UpdateCatalogOfferRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateCatalogOfferCommandUseCase
                .handle(commandMapper.toCommand(offerId, request, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/products/{productId}")
    public Mono<ProductResponse> getProduct(@PathVariable String productId, Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getProductByIdQueryUseCase
                .handle(queryMapper.toGetProductByIdQuery(productId, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/products/{productId}/detail")
    public Mono<ProductDetailResponse> getProductDetail(
            @PathVariable String productId,
            @RequestParam(name = "at", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant at,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getProductDetailQueryUseCase
                .handle(queryMapper.toGetProductDetailQuery(productId, at, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/products/{productId}/variants")
    public Flux<VariantResponse> listVariantsByProduct(
            @PathVariable String productId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listVariantsByProductQueryUseCase
                .handle(queryMapper.toListVariantsByProductQuery(productId, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/search")
    public Mono<CatalogSearchResponse> searchCatalog(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "brandId", required = false) String brandId,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "variantStatus", required = false) String variantStatus,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "at", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant at,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return searchCatalogQueryUseCase
                .handle(queryMapper.toSearchCatalogQuery(text, brandId, categoryId, variantStatus, page, size, at, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/checkout/variant-resolution")
    public Mono<CheckoutVariantResolutionResponse> resolveVariantForCheckout(
            @RequestParam("sku") String sku,
            @RequestParam(name = "currency", defaultValue = "COP") String currency,
            @RequestParam(name = "priceType", defaultValue = "BASE") String priceType,
            @RequestParam(name = "at", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant at,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return resolveVariantForCheckoutQueryUseCase
                .handle(queryMapper.toResolveVariantForCheckoutQuery(sku, currency, priceType, at, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/variants/{variantId}/prices/current")
    public Mono<PriceResponse> resolveCurrentPrice(
            @PathVariable String variantId,
            @RequestParam(name = "currency", defaultValue = "COP") String currency,
            @RequestParam(name = "priceType", defaultValue = "BASE") String priceType,
            @RequestParam(name = "at", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant at,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return resolveCurrentPriceQueryUseCase
                .handle(queryMapper.toResolveCurrentPriceQuery(variantId, currency, priceType, at, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/variants/{variantId}/prices/timeline")
    public Mono<PriceTimelineResponse> getPriceTimeline(
            @PathVariable String variantId,
            @RequestParam(name = "currency", defaultValue = "COP") String currency,
            @RequestParam(name = "priceType", defaultValue = "BASE") String priceType,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getPriceTimelineQueryUseCase
                .handle(queryMapper.toGetPriceTimelineQuery(variantId, currency, priceType, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CATALOG_ADMIN','ROLE_ARKA_ADMIN','ROLE_TRUSTED_SERVICE')")
    @GetMapping("/audits")
    public Mono<CatalogAuditResponse> getCatalogAudit(
            @RequestParam(name = "targetType", required = false) String targetType,
            @RequestParam(name = "targetId", required = false) String targetId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getCatalogAuditQueryUseCase
                .handle(queryMapper.toGetCatalogAuditQuery(targetType, targetId, page, size, principal))
                .map(responseMapper::toResponse);
    }
}

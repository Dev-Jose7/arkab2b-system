package com.arka.catalog.infrastructure.adapter.in.web.controller;

import com.arka.catalog.application.port.in.ResolveVariantForCheckoutQueryUseCase;
import com.arka.catalog.infrastructure.adapter.in.web.mapper.query.CatalogQueryMapper;
import com.arka.catalog.infrastructure.adapter.in.web.mapper.response.CatalogResponseMapper;
import com.arka.catalog.infrastructure.adapter.in.web.response.CheckoutVariantResolutionResponse;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/internal/catalog")
public class CatalogInternalHttpController {

    private final ResolveVariantForCheckoutQueryUseCase resolveVariantForCheckoutQueryUseCase;
    private final CatalogQueryMapper queryMapper;
    private final CatalogResponseMapper responseMapper;

    public CatalogInternalHttpController(
            ResolveVariantForCheckoutQueryUseCase resolveVariantForCheckoutQueryUseCase,
            CatalogQueryMapper queryMapper,
            CatalogResponseMapper responseMapper) {
        this.resolveVariantForCheckoutQueryUseCase = resolveVariantForCheckoutQueryUseCase;
        this.queryMapper = queryMapper;
        this.responseMapper = responseMapper;
    }

    @PreAuthorize("hasRole('TRUSTED_SERVICE') and hasAuthority('catalog.read')")
    @GetMapping("/checkout/variant-resolution")
    public Mono<CheckoutVariantResolutionResponse> resolveVariantForCheckoutInternal(
            @RequestParam(name = "organizationId", required = false) String organizationId,
            @RequestParam("sku") String sku,
            @RequestParam(name = "currency", defaultValue = "COP") String currency,
            @RequestParam(name = "priceType", defaultValue = "BASE") String priceType,
            @RequestParam(name = "at", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant at) {
        return resolveVariantForCheckoutQueryUseCase
                .handle(queryMapper.toResolveVariantForCheckoutQuery(
                        normalizeOrganizationId(organizationId),
                        sku,
                        currency,
                        priceType,
                        at))
                .map(responseMapper::toResponse);
    }

    private String normalizeOrganizationId(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return null;
        }
        return organizationId.trim();
    }
}

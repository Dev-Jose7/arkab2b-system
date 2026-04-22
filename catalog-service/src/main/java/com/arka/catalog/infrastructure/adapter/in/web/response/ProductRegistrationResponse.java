package com.arka.catalog.infrastructure.adapter.in.web.response;

public record ProductRegistrationResponse(
        String message,
        ProductResponse product,
        VariantResponse variant,
        PriceResponse price,
        CatalogOfferResponse offer,
        RegisteredStockResponse stock) {
}

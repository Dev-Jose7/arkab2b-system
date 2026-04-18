package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.UpdateCatalogOfferCommand;
import com.arka.catalog.application.result.CatalogOfferResult;
import reactor.core.publisher.Mono;

public interface UpdateCatalogOfferCommandUseCase {

    Mono<CatalogOfferResult> handle(UpdateCatalogOfferCommand command);
}

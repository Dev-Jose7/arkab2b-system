package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.PublishCatalogOfferCommand;
import com.arka.catalog.application.result.CatalogOfferResult;
import reactor.core.publisher.Mono;

public interface PublishCatalogOfferCommandUseCase {

    Mono<CatalogOfferResult> handle(PublishCatalogOfferCommand command);
}

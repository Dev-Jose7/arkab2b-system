package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.CreateProductCommand;
import com.arka.catalog.application.result.ProductResult;
import reactor.core.publisher.Mono;

public interface CreateProductCommandUseCase {

    Mono<ProductResult> handle(CreateProductCommand command);
}

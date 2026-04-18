package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.UpdateProductCommand;
import com.arka.catalog.application.result.ProductResult;
import reactor.core.publisher.Mono;

public interface UpdateProductCommandUseCase {

    Mono<ProductResult> handle(UpdateProductCommand command);
}

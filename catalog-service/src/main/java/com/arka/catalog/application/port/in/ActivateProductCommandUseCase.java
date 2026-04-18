package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.ActivateProductCommand;
import com.arka.catalog.application.result.ProductResult;
import reactor.core.publisher.Mono;

public interface ActivateProductCommandUseCase {

    Mono<ProductResult> handle(ActivateProductCommand command);
}

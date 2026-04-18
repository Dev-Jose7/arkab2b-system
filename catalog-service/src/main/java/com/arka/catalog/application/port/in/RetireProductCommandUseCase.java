package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.RetireProductCommand;
import com.arka.catalog.application.result.ProductResult;
import reactor.core.publisher.Mono;

public interface RetireProductCommandUseCase {

    Mono<ProductResult> handle(RetireProductCommand command);
}

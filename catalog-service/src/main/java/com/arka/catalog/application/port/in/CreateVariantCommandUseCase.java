package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.CreateVariantCommand;
import com.arka.catalog.application.result.VariantResult;
import reactor.core.publisher.Mono;

public interface CreateVariantCommandUseCase {

    Mono<VariantResult> handle(CreateVariantCommand command);
}

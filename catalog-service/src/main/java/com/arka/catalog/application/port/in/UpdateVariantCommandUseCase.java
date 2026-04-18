package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.UpdateVariantCommand;
import com.arka.catalog.application.result.VariantResult;
import reactor.core.publisher.Mono;

public interface UpdateVariantCommandUseCase {

    Mono<VariantResult> handle(UpdateVariantCommand command);
}

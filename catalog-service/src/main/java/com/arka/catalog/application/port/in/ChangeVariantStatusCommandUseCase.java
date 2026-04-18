package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.ChangeVariantStatusCommand;
import com.arka.catalog.application.result.VariantResult;
import reactor.core.publisher.Mono;

public interface ChangeVariantStatusCommandUseCase {

    Mono<VariantResult> handle(ChangeVariantStatusCommand command);
}

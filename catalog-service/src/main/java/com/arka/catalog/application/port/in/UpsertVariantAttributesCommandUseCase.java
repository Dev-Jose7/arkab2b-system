package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.UpsertVariantAttributesCommand;
import com.arka.catalog.application.result.VariantResult;
import reactor.core.publisher.Mono;

public interface UpsertVariantAttributesCommandUseCase {

    Mono<VariantResult> handle(UpsertVariantAttributesCommand command);
}

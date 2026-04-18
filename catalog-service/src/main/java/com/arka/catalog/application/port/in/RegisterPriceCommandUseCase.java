package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.RegisterPriceCommand;
import com.arka.catalog.application.result.PriceResult;
import reactor.core.publisher.Mono;

public interface RegisterPriceCommandUseCase {

    Mono<PriceResult> handle(RegisterPriceCommand command);
}

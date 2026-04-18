package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.UpdatePriceCommand;
import com.arka.catalog.application.result.PriceResult;
import reactor.core.publisher.Mono;

public interface UpdatePriceCommandUseCase {

    Mono<PriceResult> handle(UpdatePriceCommand command);
}

package com.arka.catalog.application.port.in;

import com.arka.catalog.application.command.SchedulePriceActivationCommand;
import reactor.core.publisher.Mono;

public interface SchedulePriceActivationCommandUseCase {

    Mono<Void> handle(SchedulePriceActivationCommand command);
}

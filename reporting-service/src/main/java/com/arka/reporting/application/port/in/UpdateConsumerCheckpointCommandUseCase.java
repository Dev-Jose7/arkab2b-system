package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.UpdateConsumerCheckpointCommand;
import reactor.core.publisher.Mono;

public interface UpdateConsumerCheckpointCommandUseCase {

    Mono<Void> handle(UpdateConsumerCheckpointCommand command);
}

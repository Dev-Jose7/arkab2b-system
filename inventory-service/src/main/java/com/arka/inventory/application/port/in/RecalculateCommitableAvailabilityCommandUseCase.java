package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.RecalculateCommitableAvailabilityCommand;
import com.arka.inventory.application.result.CommitableAvailabilityResult;
import reactor.core.publisher.Mono;

public interface RecalculateCommitableAvailabilityCommandUseCase {

    Mono<CommitableAvailabilityResult> handle(RecalculateCommitableAvailabilityCommand command);
}

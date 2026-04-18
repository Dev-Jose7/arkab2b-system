package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.RebuildProjectionCommand;
import com.arka.reporting.application.result.WeeklyExecutionResult;
import reactor.core.publisher.Mono;

public interface RebuildProjectionCommandUseCase {

    Mono<WeeklyExecutionResult> handle(RebuildProjectionCommand command);
}

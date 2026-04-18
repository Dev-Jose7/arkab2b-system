package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.RegisterAnalyticFactCommand;
import com.arka.reporting.application.result.AnalyticFactResult;
import reactor.core.publisher.Mono;

public interface RegisterAnalyticFactCommandUseCase {

    Mono<AnalyticFactResult> handle(RegisterAnalyticFactCommand command);
}

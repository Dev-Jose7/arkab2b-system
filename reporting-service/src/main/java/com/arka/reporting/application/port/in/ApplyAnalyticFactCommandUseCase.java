package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.ApplyAnalyticFactCommand;
import com.arka.reporting.application.result.AnalyticFactResult;
import reactor.core.publisher.Mono;

public interface ApplyAnalyticFactCommandUseCase {

    Mono<AnalyticFactResult> handle(ApplyAnalyticFactCommand command);
}

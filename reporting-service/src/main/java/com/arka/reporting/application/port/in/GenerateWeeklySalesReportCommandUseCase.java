package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.GenerateWeeklySalesReportCommand;
import com.arka.reporting.application.result.WeeklyExecutionResult;
import reactor.core.publisher.Mono;

public interface GenerateWeeklySalesReportCommandUseCase {

    Mono<WeeklyExecutionResult> handle(GenerateWeeklySalesReportCommand command);
}

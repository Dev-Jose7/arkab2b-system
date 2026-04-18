package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.GenerateWeeklyReplenishmentReportCommand;
import com.arka.reporting.application.result.WeeklyExecutionResult;
import reactor.core.publisher.Mono;

public interface GenerateWeeklyReplenishmentReportCommandUseCase {

    Mono<WeeklyExecutionResult> handle(GenerateWeeklyReplenishmentReportCommand command);
}

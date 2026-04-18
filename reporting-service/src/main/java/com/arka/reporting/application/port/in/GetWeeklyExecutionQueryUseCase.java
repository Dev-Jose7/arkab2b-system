package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.GetWeeklyExecutionQuery;
import com.arka.reporting.application.result.WeeklyExecutionResult;
import reactor.core.publisher.Mono;

public interface GetWeeklyExecutionQueryUseCase {

    Mono<WeeklyExecutionResult> handle(GetWeeklyExecutionQuery query);
}

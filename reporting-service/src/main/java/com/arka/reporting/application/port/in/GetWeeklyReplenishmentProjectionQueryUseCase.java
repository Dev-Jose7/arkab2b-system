package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.GetWeeklyReplenishmentProjectionQuery;
import com.arka.reporting.application.result.FactSearchResult;
import com.arka.reporting.application.result.ReplenishmentProjectionResult;
import reactor.core.publisher.Flux;

public interface GetWeeklyReplenishmentProjectionQueryUseCase {

    Flux<ReplenishmentProjectionResult> handle(GetWeeklyReplenishmentProjectionQuery query);
}

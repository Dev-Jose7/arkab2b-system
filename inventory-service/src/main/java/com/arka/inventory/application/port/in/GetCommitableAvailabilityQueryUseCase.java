package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.GetCommitableAvailabilityQuery;
import com.arka.inventory.application.result.CommitableAvailabilityResult;
import reactor.core.publisher.Mono;

public interface GetCommitableAvailabilityQueryUseCase {

    Mono<CommitableAvailabilityResult> handle(GetCommitableAvailabilityQuery query);
}

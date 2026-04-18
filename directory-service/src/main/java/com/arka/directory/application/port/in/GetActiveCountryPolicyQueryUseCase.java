package com.arka.directory.application.port.in;

import com.arka.directory.application.query.GetActiveCountryPolicyQuery;
import com.arka.directory.application.result.CountryPolicyResult;
import reactor.core.publisher.Mono;

public interface GetActiveCountryPolicyQueryUseCase {

    Mono<CountryPolicyResult> handle(GetActiveCountryPolicyQuery query);
}

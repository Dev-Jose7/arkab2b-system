package com.arka.directory.application.port.in;

import com.arka.directory.application.command.ConfigureRegionalPolicyCommand;
import com.arka.directory.application.result.CountryPolicyResult;
import reactor.core.publisher.Mono;

public interface ConfigureRegionalPolicyCommandUseCase {

    Mono<CountryPolicyResult> handle(ConfigureRegionalPolicyCommand command);
}

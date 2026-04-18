package com.arka.directory.application.port.in;

import com.arka.directory.application.command.ApplyRegionalPolicyToOperationCommand;
import com.arka.directory.application.result.RegionalPolicyApplicationResult;
import reactor.core.publisher.Mono;

public interface ApplyRegionalPolicyToOperationCommandUseCase {

    Mono<RegionalPolicyApplicationResult> handle(ApplyRegionalPolicyToOperationCommand command);
}

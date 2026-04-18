package com.arka.directory.application.port.in;

import com.arka.directory.application.command.HandleIamUserBlockedCommand;
import com.arka.directory.application.result.OrganizationUserProfileResult;
import reactor.core.publisher.Mono;

public interface HandleIamUserBlockedCommandUseCase {

    Mono<OrganizationUserProfileResult> handle(HandleIamUserBlockedCommand command);
}

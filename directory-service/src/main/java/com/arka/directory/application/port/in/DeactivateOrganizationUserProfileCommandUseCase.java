package com.arka.directory.application.port.in;

import com.arka.directory.application.command.DeactivateOrganizationUserProfileCommand;
import com.arka.directory.application.result.OrganizationUserProfileResult;
import reactor.core.publisher.Mono;

public interface DeactivateOrganizationUserProfileCommandUseCase {

    Mono<OrganizationUserProfileResult> handle(DeactivateOrganizationUserProfileCommand command);
}

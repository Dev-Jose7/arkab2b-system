package com.arka.directory.application.port.in;

import com.arka.directory.application.command.DeactivateOrganizationContactCommand;
import com.arka.directory.application.result.OrganizationContactResult;
import reactor.core.publisher.Mono;

public interface DeactivateOrganizationContactCommandUseCase {

    Mono<OrganizationContactResult> handle(DeactivateOrganizationContactCommand command);
}

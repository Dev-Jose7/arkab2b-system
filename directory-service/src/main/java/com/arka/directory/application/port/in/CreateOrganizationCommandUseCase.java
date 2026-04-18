package com.arka.directory.application.port.in;

import com.arka.directory.application.command.CreateOrganizationCommand;
import com.arka.directory.application.result.OrganizationResult;
import reactor.core.publisher.Mono;

public interface CreateOrganizationCommandUseCase {

    Mono<OrganizationResult> handle(CreateOrganizationCommand command);
}

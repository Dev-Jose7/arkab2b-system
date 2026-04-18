package com.arka.directory.application.port.in;

import com.arka.directory.application.command.UpdateOrganizationStatusCommand;
import com.arka.directory.application.result.OrganizationResult;
import reactor.core.publisher.Mono;

public interface UpdateOrganizationStatusCommandUseCase {

    Mono<OrganizationResult> handle(UpdateOrganizationStatusCommand command);
}

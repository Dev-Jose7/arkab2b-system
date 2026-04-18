package com.arka.directory.application.port.in;

import com.arka.directory.application.command.UpsertOrganizationContactCommand;
import com.arka.directory.application.result.OrganizationContactResult;
import reactor.core.publisher.Mono;

public interface UpsertOrganizationContactCommandUseCase {

    Mono<OrganizationContactResult> handle(UpsertOrganizationContactCommand command);
}

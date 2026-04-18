package com.arka.directory.application.port.in;

import com.arka.directory.application.command.UpsertOrganizationLegalProfileCommand;
import com.arka.directory.application.result.OrganizationLegalProfileResult;
import reactor.core.publisher.Mono;

public interface UpsertOrganizationLegalProfileCommandUseCase {

    Mono<OrganizationLegalProfileResult> handle(UpsertOrganizationLegalProfileCommand command);
}

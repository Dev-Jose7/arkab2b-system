package com.arka.directory.application.port.in;

import com.arka.directory.application.command.UpsertOrganizationUserProfileCommand;
import com.arka.directory.application.result.OrganizationUserProfileResult;
import reactor.core.publisher.Mono;

public interface UpsertOrganizationUserProfileCommandUseCase {

    Mono<OrganizationUserProfileResult> handle(UpsertOrganizationUserProfileCommand command);
}

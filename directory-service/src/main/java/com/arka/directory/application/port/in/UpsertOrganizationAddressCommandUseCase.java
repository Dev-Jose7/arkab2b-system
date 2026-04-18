package com.arka.directory.application.port.in;

import com.arka.directory.application.command.UpsertAddressCommand;
import com.arka.directory.application.result.AddressResult;
import reactor.core.publisher.Mono;

public interface UpsertOrganizationAddressCommandUseCase {

    Mono<AddressResult> handle(UpsertAddressCommand command);
}

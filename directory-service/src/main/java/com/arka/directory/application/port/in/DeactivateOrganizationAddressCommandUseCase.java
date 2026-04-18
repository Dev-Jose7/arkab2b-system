package com.arka.directory.application.port.in;

import com.arka.directory.application.command.DeactivateAddressCommand;
import com.arka.directory.application.result.AddressResult;
import reactor.core.publisher.Mono;

public interface DeactivateOrganizationAddressCommandUseCase {

    Mono<AddressResult> handle(DeactivateAddressCommand command);
}

package com.arka.directory.application.port.in;

import com.arka.directory.application.command.MarkDefaultAddressCommand;
import com.arka.directory.application.result.AddressResult;
import reactor.core.publisher.Mono;

public interface MarkDefaultOrganizationAddressCommandUseCase {

    Mono<AddressResult> handle(MarkDefaultAddressCommand command);
}

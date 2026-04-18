package com.arka.directory.application.port.out.external;

import reactor.core.publisher.Mono;

public interface TaxValidationPort {

    Mono<Boolean> isTaxIdValid(String countryCode, String taxIdType, String taxId);
}

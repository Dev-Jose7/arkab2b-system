package com.arka.directory.application.port.out.external;

import reactor.core.publisher.Mono;

public interface GeoValidationPort {

    Mono<Boolean> isAddressValid(String countryCode, String city, String postalCode, String line1);
}

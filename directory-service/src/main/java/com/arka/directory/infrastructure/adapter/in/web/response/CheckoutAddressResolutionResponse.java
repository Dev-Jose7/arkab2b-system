package com.arka.directory.infrastructure.adapter.in.web.response;

public record CheckoutAddressResolutionResponse(
        AddressResponse address,
        CountryPolicyResponse countryPolicy,
        String resolutionStatus) {}

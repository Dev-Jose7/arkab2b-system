package com.arka.directory.application.result;

public record CheckoutAddressResolutionResult(
        AddressResult address,
        CountryPolicyResult countryPolicy,
        String resolutionStatus) {}

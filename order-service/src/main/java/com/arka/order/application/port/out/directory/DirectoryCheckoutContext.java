package com.arka.order.application.port.out.directory;

public record DirectoryCheckoutContext(
        String organizationId,
        String addressId,
        String countryCode,
        long regionalPolicyVersion,
        String policyCurrency,
        boolean policyActive,
        boolean addressValid) {}

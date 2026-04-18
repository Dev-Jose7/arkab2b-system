package com.arka.directory.domain.organizationcontext.valueobject;

import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;

public record CountryCode(String value) {

    public CountryCode {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("countryCode is required");
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.length() != 2) {
            throw new DomainInvariantViolationException("countryCode must contain 2 chars");
        }
        value = normalized;
    }

    public static CountryCode of(String value) {
        return new CountryCode(value);
    }
}

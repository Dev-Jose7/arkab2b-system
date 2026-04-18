package com.arka.directory.domain.organizationcontext.enumtype;

public enum AddressValidationStatus {
    PENDING,
    VERIFIED,
    REJECTED;

    public boolean isVerified() {
        return this == VERIFIED;
    }
}

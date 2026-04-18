package com.arka.directory.domain.organizationcontext.enumtype;

public enum AddressStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}

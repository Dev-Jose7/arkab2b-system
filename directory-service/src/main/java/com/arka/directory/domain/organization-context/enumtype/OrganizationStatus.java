package com.arka.directory.domain.organizationcontext.enumtype;

public enum OrganizationStatus {
    ONBOARDING,
    ACTIVE,
    SUSPENDED,
    INACTIVE;

    public boolean allowsProfileMutations() {
        return this == ONBOARDING || this == ACTIVE || this == SUSPENDED;
    }

    public boolean isOperational() {
        return this == ACTIVE;
    }
}

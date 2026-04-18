package com.arka.directory.domain.countrypolicy.enumtype;

public enum CountryPolicyStatus {
    ACTIVE,
    INACTIVE,
    SUPERSEDED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}

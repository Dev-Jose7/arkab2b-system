package com.arka.directory.domain.organizationcontext.enumtype;

public enum OrganizationContactStatus {
    ACTIVE,
    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}

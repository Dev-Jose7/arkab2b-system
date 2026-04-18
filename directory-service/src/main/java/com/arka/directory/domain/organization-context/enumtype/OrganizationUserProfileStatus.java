package com.arka.directory.domain.organizationcontext.enumtype;

public enum OrganizationUserProfileStatus {
    ACTIVE,
    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}

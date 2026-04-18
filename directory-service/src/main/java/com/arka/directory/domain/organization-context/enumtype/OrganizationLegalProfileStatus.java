package com.arka.directory.domain.organizationcontext.enumtype;

public enum OrganizationLegalProfileStatus {
    PENDING,
    VERIFIED,
    REJECTED;

    public boolean isVerified() {
        return this == VERIFIED;
    }
}

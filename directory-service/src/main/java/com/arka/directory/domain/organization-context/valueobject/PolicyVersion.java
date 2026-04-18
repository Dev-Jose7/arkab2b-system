package com.arka.directory.domain.organizationcontext.valueobject;

import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;

public record PolicyVersion(long value) {

    public PolicyVersion {
        if (value <= 0) {
            throw new DomainInvariantViolationException("policyVersion must be positive");
        }
    }

    public static PolicyVersion of(long value) {
        return new PolicyVersion(value);
    }

    public PolicyVersion next() {
        return new PolicyVersion(value + 1);
    }
}

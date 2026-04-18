package com.arka.directory.domain.organizationcontext.exception;

public class OrganizationIsolationViolationException extends OrganizationContextException {

    public OrganizationIsolationViolationException(String message) {
        super("organization_isolation_violation", message);
    }
}

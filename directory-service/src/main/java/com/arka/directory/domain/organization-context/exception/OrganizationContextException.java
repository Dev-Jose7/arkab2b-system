package com.arka.directory.domain.organizationcontext.exception;

import com.arka.directory.domain.shared.exception.DomainException;

public class OrganizationContextException extends DomainException {

    public OrganizationContextException(String code, String message) {
        super(code, message);
    }
}

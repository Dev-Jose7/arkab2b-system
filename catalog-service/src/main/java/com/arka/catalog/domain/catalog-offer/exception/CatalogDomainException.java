package com.arka.catalog.domain.catalogoffer.exception;

import com.arka.catalog.domain.shared.exception.DomainException;

public class CatalogDomainException extends DomainException {

    public CatalogDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}

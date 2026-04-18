package com.arka.catalog.domain.catalogoffer.exception;

public class InvalidPriceException extends CatalogDomainException {

    public InvalidPriceException(String message) {
        super("precio_invalido", message);
    }
}

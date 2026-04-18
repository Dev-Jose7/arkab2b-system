package com.arka.catalog.domain.catalogoffer.exception;

public class RequiredAttributesMissingException extends CatalogDomainException {

    public RequiredAttributesMissingException() {
        super("required_attributes_missing", "La variante no cumple atributos minimos requeridos");
    }
}

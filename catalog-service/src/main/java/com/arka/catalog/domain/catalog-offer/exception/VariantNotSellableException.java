package com.arka.catalog.domain.catalogoffer.exception;

public class VariantNotSellableException extends CatalogDomainException {

    public VariantNotSellableException() {
        super("variante_no_vendible", "La variante no esta vendible o no tiene precio vigente");
    }
}

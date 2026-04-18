package com.arka.catalog.domain.catalogoffer.exception;

public class ProductNotActiveException extends CatalogDomainException {

    public ProductNotActiveException() {
        super("producto_no_activo", "La variante vendible requiere producto ACTIVE");
    }
}

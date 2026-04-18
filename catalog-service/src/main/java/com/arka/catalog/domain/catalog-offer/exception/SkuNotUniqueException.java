package com.arka.catalog.domain.catalogoffer.exception;

public class SkuNotUniqueException extends CatalogDomainException {

    public SkuNotUniqueException(String sku) {
        super("sku_no_unico", "El SKU ya esta en uso para una variante vendible: " + sku);
    }
}

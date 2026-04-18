package com.arka.catalog.application.exception;

public class CatalogResourceNotFoundException extends ApplicationException {

    public CatalogResourceNotFoundException(String resource, String id) {
        super("catalog_recurso_no_encontrado", resource + " no encontrado: " + id);
    }
}

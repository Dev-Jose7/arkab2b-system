package com.arka.catalog.domain.catalogoffer.exception;

public class CrossTenantAccessException extends CatalogDomainException {

    public CrossTenantAccessException() {
        super("acceso_cruzado_detectado", "El recurso comercial pertenece a otro tenant");
    }
}

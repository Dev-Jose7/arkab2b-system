package com.arka.catalog.domain.catalogoffer.exception;

public class CrossOrganizationAccessException extends CatalogDomainException {

    public CrossOrganizationAccessException() {
        super("acceso_cruzado_detectado", "El recurso comercial pertenece a otro organization");
    }
}

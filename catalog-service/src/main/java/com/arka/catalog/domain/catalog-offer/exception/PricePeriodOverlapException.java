package com.arka.catalog.domain.catalogoffer.exception;

public class PricePeriodOverlapException extends CatalogDomainException {

    public PricePeriodOverlapException() {
        super("periodo_precio_solapado", "No se permiten periodos de precio solapados para la misma variante/moneda/tipo");
    }
}

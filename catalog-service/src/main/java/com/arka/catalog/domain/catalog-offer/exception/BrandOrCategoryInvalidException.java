package com.arka.catalog.domain.catalogoffer.exception;

public class BrandOrCategoryInvalidException extends CatalogDomainException {

    public BrandOrCategoryInvalidException() {
        super("brand_o_categoria_invalida", "La marca o categoria no es valida/activa para el tenant");
    }
}

package com.juanamaris.inmobiliaria.model;

public enum TipoBusquedaCompanero {
    TENGO_CUARTO("Tengo cuarto, busco compañero"),
    BUSCO_CUARTO("Busco cuarto y compañero");

    private final String etiqueta;
    TipoBusquedaCompanero(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

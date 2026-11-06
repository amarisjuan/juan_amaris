package com.juanamaris.inmobiliaria.model;

public enum ObjetivoReporte {
    INMUEBLE("Anuncio de inmueble"), COMPANERO("Perfil de compañero"), PERFIL("Perfil de anunciante");

    private final String etiqueta;
    ObjetivoReporte(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

package com.juanamaris.inmobiliaria.model;

/** Quién puede vivir en el inmueble. */
public enum Convivencia {
    MIXTO("Mixto"), SOLO_MUJERES("Solo mujeres"), SOLO_HOMBRES("Solo hombres");

    private final String etiqueta;
    Convivencia(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

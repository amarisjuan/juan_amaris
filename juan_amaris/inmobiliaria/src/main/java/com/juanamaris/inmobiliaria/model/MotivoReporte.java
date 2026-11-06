package com.juanamaris.inmobiliaria.model;

/** Motivos de reporte (consulta legal 003). Los graves cuentan para el ocultamiento temporal automático. */
public enum MotivoReporte {
    INMUEBLE_FALSO("Inmueble falso o inexistente", true),
    ESTAFA("Estafa o cobro anticipado", true),
    USA_MIS_DATOS("Usa mis datos o mis fotos", true),
    DISCRIMINACION("Discriminación", false),
    OTRO("Otro", false);

    private final String etiqueta;
    private final boolean grave;
    MotivoReporte(String etiqueta, boolean grave) { this.etiqueta = etiqueta; this.grave = grave; }
    public String getEtiqueta() { return etiqueta; }
    public boolean isGrave() { return grave; }
}

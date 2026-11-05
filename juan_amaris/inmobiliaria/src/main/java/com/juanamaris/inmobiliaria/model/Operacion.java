package com.juanamaris.inmobiliaria.model;

public enum Operacion {
    ARRIENDO("Arriendo"), VENTA("Venta");

    private final String etiqueta;
    Operacion(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

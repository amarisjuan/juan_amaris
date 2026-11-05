package com.juanamaris.inmobiliaria.model;

public enum TipoInmueble {
    APARTAMENTO("Apartamento"), CASA("Casa"), APARTAESTUDIO("Apartaestudio"), HABITACION("Habitación"),
    LOCAL("Local comercial"), OFICINA("Oficina"), BODEGA("Bodega"), LOTE("Lote"), FINCA("Finca");

    private final String etiqueta;
    TipoInmueble(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

package com.juanamaris.inmobiliaria.model;

public enum Genero {
    MUJER("Mujer"), HOMBRE("Hombre");

    private final String etiqueta;
    Genero(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

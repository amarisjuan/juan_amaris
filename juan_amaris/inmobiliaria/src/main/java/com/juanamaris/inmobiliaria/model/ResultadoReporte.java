package com.juanamaris.inmobiliaria.model;

public enum ResultadoReporte {
    SIN_INCUMPLIMIENTO("No se encontró incumplimiento"), SE_CORRIGE("Se pidió corregir el anuncio"),
    SE_RETIRA("Se retiró el anuncio"), CUENTA_BLOQUEADA("Se bloqueó la cuenta");

    private final String etiqueta;
    ResultadoReporte(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

package com.juanamaris.inmobiliaria.model;

public enum TipoDocumento {
    CC("Cédula de ciudadanía"), CE("Cédula de extranjería"), PPT("Permiso por Protección Temporal"),
    PASAPORTE("Pasaporte"), NIT("NIT (empresa)");

    private final String etiqueta;
    TipoDocumento(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}

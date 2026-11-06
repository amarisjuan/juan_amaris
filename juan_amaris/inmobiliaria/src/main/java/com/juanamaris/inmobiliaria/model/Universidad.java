package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "universidades")
public class Universidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    /** Nombre corto con el que la gente la busca, ej. "Uninorte". */
    private String sigla;

    /** Sitio web oficial (SNIES), si lo tenemos. */
    private String web;

    @Column(nullable = false)
    private String ciudad;

    /** Departamento del municipio (DIVIPOLA). Puede ser nulo en registros anteriores hasta el próximo arranque. */
    @Column(length = 60)
    private String departamento;

    private Double latitud;
    private Double longitud;

    protected Universidad() {}

    public Universidad(String nombre, String sigla, String ciudad) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.ciudad = ciudad;
    }

    public void ubicar(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /** Ubicación del centro del municipio (no del campus): se muestra como aproximada. */
    public void ubicarAproximada(Double latitud, Double longitud) { this.latitud = latitud; this.longitud = longitud; }

    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web == null || web.isBlank() ? null : web.trim(); }

    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public boolean isUbicada() { return latitud != null && longitud != null; }

    public String getNombreCorto() { return sigla != null ? sigla : nombre; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSigla() { return sigla; }
    public String getCiudad() { return ciudad; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}

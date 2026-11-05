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

    @Column(nullable = false)
    private String ciudad;

    protected Universidad() {}

    public Universidad(String nombre, String sigla, String ciudad) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.ciudad = ciudad;
    }

    public String getNombreCorto() { return sigla != null ? sigla : nombre; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSigla() { return sigla; }
    public String getCiudad() { return ciudad; }
}

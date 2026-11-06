package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;

/** Barrio conocido de una ciudad: se aprende de las publicaciones y sirve de sugerencia en los filtros. */
@Entity
@Table(name = "barrios", uniqueConstraints = @UniqueConstraint(columnNames = {"ciudad", "nombre"}))
public class Barrio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String ciudad;

    @Column(nullable = false, length = 80)
    private String nombre;

    protected Barrio() {}

    public Barrio(String ciudad, String nombre) {
        this.ciudad = ciudad;
        this.nombre = nombre;
    }

    public String getCiudad() { return ciudad; }
    public String getNombre() { return nombre; }
}

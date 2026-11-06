package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Contador agregado por día. No guarda IP, usuario ni cookies: solo cuántas veces pasó algo.
 * clave: id del inmueble (contactos, vistas) o la descripción de una búsqueda; vacía para totales.
 */
@Entity
@Table(name = "conteos_diarios", uniqueConstraints = @UniqueConstraint(columnNames = {"fecha", "tipo", "clave"}),
        indexes = @Index(columnList = "fecha"))
public class ConteoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEvento tipo;

    @Column(nullable = false, length = 200)
    private String clave = "";

    private long cantidad;

    protected ConteoDiario() {}

    public ConteoDiario(LocalDate fecha, TipoEvento tipo, String clave) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.clave = clave;
    }

    public void sumar() { cantidad++; }
    public LocalDate getFecha() { return fecha; }
    public TipoEvento getTipo() { return tipo; }
    public String getClave() { return clave; }
    public long getCantidad() { return cantidad; }
}

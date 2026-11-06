package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Correo o documento vetado: no puede crear cuentas, ingresar ni registrarse como anunciante. */
@Entity
@Table(name = "bloqueos", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "valor"}))
public class Bloqueo {

    public enum Tipo { CORREO, DOCUMENTO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    /** Correo en minúsculas o documento solo con letras y números (sin puntos ni guiones). */
    @Column(nullable = false, length = 160)
    private String valor;

    @Column(length = 500)
    private String motivo;

    private String bloqueadoPor;
    private LocalDateTime fecha = LocalDateTime.now();

    protected Bloqueo() {}

    public Bloqueo(Tipo tipo, String valor, String motivo, String bloqueadoPor) {
        this.tipo = tipo;
        this.valor = valor;
        this.motivo = motivo;
        this.bloqueadoPor = bloqueadoPor;
    }

    public static String normalizarCorreo(String c) { return c == null ? "" : c.trim().toLowerCase(); }
    public static String normalizarDocumento(String d) { return d == null ? "" : d.replaceAll("[^0-9A-Za-z]", "").toUpperCase(); }

    public Long getId() { return id; }
    public Tipo getTipo() { return tipo; }
    public String getValor() { return valor; }
    public String getMotivo() { return motivo; }
    public String getBloqueadoPor() { return bloqueadoPor; }
    public LocalDateTime getFecha() { return fecha; }
}

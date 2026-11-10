package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Enlace de recuperación de contraseña. Solo se guarda el hash SHA-256 del código: quien lea la base
 * de datos no puede usar los enlaces. Sirve una vez y vence.
 */
@Entity
@Table(name = "tokens_recuperacion", indexes = @Index(columnList = "hash", unique = true))
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Usuario usuario;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @Column(nullable = false)
    private LocalDateTime venceEn;

    private LocalDateTime usadoEn;

    protected TokenRecuperacion() { }

    public TokenRecuperacion(Usuario usuario, String hash, LocalDateTime creadoEn, LocalDateTime venceEn) {
        this.usuario = usuario;
        this.hash = hash;
        this.creadoEn = creadoEn;
        this.venceEn = venceEn;
    }

    public boolean vigente(LocalDateTime ahora) { return usadoEn == null && ahora.isBefore(venceEn); }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getHash() { return hash; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getVenceEn() { return venceEn; }
    public void setVenceEn(LocalDateTime venceEn) { this.venceEn = venceEn; }
    public LocalDateTime getUsadoEn() { return usadoEn; }
    public void setUsadoEn(LocalDateTime usadoEn) { this.usadoEn = usadoEn; }
}

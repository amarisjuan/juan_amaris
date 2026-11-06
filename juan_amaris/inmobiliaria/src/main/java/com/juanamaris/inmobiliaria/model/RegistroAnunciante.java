package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Registro privado de quienes publican inmuebles (Ley 1480 de 2011, art. 53: portales de contacto).
 * NUNCA se muestra en vistas públicas: solo lo ve el administrador. No registrar estos datos en logs.
 */
@Entity
@Table(name = "registro_anunciantes")
public class RegistroAnunciante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true)
    private Usuario usuario;

    @NotNull(message = "Elige el tipo de documento")
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "Escribe el número de documento")
    @Pattern(regexp = "[0-9A-Za-z.\\-]{4,20}", message = "Número de documento no válido")
    private String numeroDocumento;

    @NotBlank(message = "Escribe la dirección de notificaciones")
    @Size(max = 150)
    private String direccionNotificaciones;

    @NotBlank(message = "Escribe la ciudad")
    @Size(max = 60)
    private String ciudadNotificaciones;

    private LocalDateTime fechaRegistro = LocalDateTime.now();
    private LocalDateTime fechaActualizacion;

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String n) { this.numeroDocumento = n == null ? null : n.trim(); }
    public String getDireccionNotificaciones() { return direccionNotificaciones; }
    public void setDireccionNotificaciones(String d) { this.direccionNotificaciones = d == null ? null : d.trim(); }
    public String getCiudadNotificaciones() { return ciudadNotificaciones; }
    public void setCiudadNotificaciones(String c) { this.ciudadNotificaciones = c == null ? null : c.trim(); }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime f) { this.fechaActualizacion = f; }

    @Override
    public String toString() { return "RegistroAnunciante[id=" + id + "]"; } // sin datos personales
}

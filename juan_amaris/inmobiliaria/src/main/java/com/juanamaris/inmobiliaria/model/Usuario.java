package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 80)
    private String nombre;

    @NotBlank(message = "Escribe tu correo")
    @Email(message = "Correo no válido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Escribe tu celular")
    @Pattern(regexp = "3\\d{9}", message = "Celular colombiano de 10 dígitos, ej. 3001234567")
    private String celular;

    @Column(nullable = false)
    private String password;

    private String rol = "USER";

    private java.time.LocalDateTime fechaRegistro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public java.time.LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(java.time.LocalDateTime f) { this.fechaRegistro = f; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}

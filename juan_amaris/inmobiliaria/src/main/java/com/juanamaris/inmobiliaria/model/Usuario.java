package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo (nombres + apellidos). Solo para uso interno y del administrador: no se publica. */
    @Size(max = 160)
    private String nombre;

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 60)
    private String nombres;

    @NotBlank(message = "Escribe tus apellidos")
    @Size(max = 60)
    private String apellidos;

    /** Si es empresa (registró NIT), se muestra la razón social completa. */
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean empresa;

    /** Cuenta bloqueada por el administrador: no puede ingresar y sus anuncios no se muestran. */
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean bloqueado;

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
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres == null ? null : nombres.trim(); }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos == null ? null : apellidos.trim(); }
    public boolean isEmpresa() { return empresa; }
    public void setEmpresa(boolean empresa) { this.empresa = empresa; }
    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    /**
     * Lo único que se publica del nombre (consulta legal 003): primer nombre + inicial del primer apellido ("Laura M.").
     * Las empresas muestran su razón social. Las cuentas antiguas sin nombres separados usan el nombre guardado.
     */
    public String getNombrePublico() {
        if (empresa) return nombre;
        String n = nombres, a = apellidos;
        if ((n == null || n.isBlank()) && nombre != null) {
            String[] partes = nombre.trim().split("\\s+");
            n = partes[0];
            a = partes.length > 1 ? partes[1] : null;
        }
        if (n == null || n.isBlank()) return "Anunciante";
        String primerNombre = n.trim().split("\\s+")[0];
        // Inicial = primera letra real del apellido (las cuentas antiguas sin apellido tienen "-")
        if (a != null) {
            for (char c : a.toCharArray()) {
                if (Character.isLetter(c)) return primerNombre + " " + Character.toUpperCase(c) + ".";
            }
        }
        return primerNombre;
    }
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

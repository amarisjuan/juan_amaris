package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** Publicación de un estudiante que busca compañero de cuarto. */
@Entity
@Table(name = "companeros")
public class Companero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario autor;

    @NotNull(message = "Elige una opción")
    @Enumerated(EnumType.STRING)
    private TipoBusquedaCompanero tipo;

    @NotNull(message = "Elige tu universidad")
    @ManyToOne
    private Universidad universidad;

    @NotBlank(message = "Escribe tu carrera")
    @Size(max = 80)
    private String carrera;

    @Min(value = 1, message = "Semestre entre 1 y 12") @Max(value = 12, message = "Semestre entre 1 y 12")
    private Integer semestre;

    @NotNull(message = "Elige tu género")
    @Enumerated(EnumType.STRING)
    private Genero genero;

    /** Con quién quiere vivir. Nulo = le da igual. */
    @Enumerated(EnumType.STRING)
    private Genero buscoGenero;

    @NotNull(message = "Escribe el presupuesto")
    @Positive(message = "El presupuesto debe ser mayor a 0")
    private Long presupuesto;

    @Size(max = 80)
    private String barrio;

    @Size(max = 60)
    private String ciudadOrigen;

    @NotBlank(message = "Cuéntales un poco de ti")
    @Size(max = 1000, message = "Máximo 1000 caracteres")
    @Column(length = 1000)
    private String descripcion;


    // --- Revisión por reportes (consulta legal 003). Oculto al público mientras está en revisión. ---
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean enRevision;
    private String motivoRevision;
    @Column(length = 2000)
    private String respuestaAnunciante;
    private java.time.LocalDateTime fechaRevision;

    private boolean activo = true;

    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    public Long getId() { return id; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public TipoBusquedaCompanero getTipo() { return tipo; }
    public void setTipo(TipoBusquedaCompanero tipo) { this.tipo = tipo; }
    public Universidad getUniversidad() { return universidad; }
    public void setUniversidad(Universidad universidad) { this.universidad = universidad; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }
    public Genero getGenero() { return genero; }
    public void setGenero(Genero genero) { this.genero = genero; }
    public Genero getBuscoGenero() { return buscoGenero; }
    public void setBuscoGenero(Genero buscoGenero) { this.buscoGenero = buscoGenero; }
    public Long getPresupuesto() { return presupuesto; }
    public void setPresupuesto(Long presupuesto) { this.presupuesto = presupuesto; }
    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio == null || barrio.isBlank() ? null : barrio.trim(); }
    public String getCiudadOrigen() { return ciudadOrigen; }
    public void setCiudadOrigen(String c) { this.ciudadOrigen = c == null || c.isBlank() ? null : c.trim(); }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }

    public boolean isEnRevision() { return enRevision; }
    public String getMotivoRevision() { return motivoRevision; }
    public String getRespuestaAnunciante() { return respuestaAnunciante; }
    public void setRespuestaAnunciante(String r) { this.respuestaAnunciante = r; }
    public java.time.LocalDateTime getFechaRevision() { return fechaRevision; }
    public void ponerEnRevision(String motivoGeneral) {
        this.enRevision = true;
        this.motivoRevision = motivoGeneral;
        this.fechaRevision = java.time.LocalDateTime.now();
        this.respuestaAnunciante = null;
    }
    public void quitarRevision() { this.enRevision = false; }
}

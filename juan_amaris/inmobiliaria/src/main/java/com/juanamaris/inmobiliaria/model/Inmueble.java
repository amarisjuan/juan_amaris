package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inmuebles", indexes = {
        @Index(columnList = "ciudad"), @Index(columnList = "operacion"), @Index(columnList = "precio")})
public class Inmueble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Escribe un título")
    @Size(max = 120)
    private String titulo;

    @Size(max = 3000)
    @Column(length = 3000)
    private String descripcion;

    @NotNull(message = "Elige el tipo")
    @Enumerated(EnumType.STRING)
    private TipoInmueble tipo;

    @NotNull(message = "Elige arriendo o venta")
    @Enumerated(EnumType.STRING)
    private Operacion operacion;

    @NotNull(message = "Escribe el precio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Long precio;

    @NotBlank(message = "Escribe la ciudad")
    private String ciudad;

    @Size(max = 80)
    private String barrio;

    @Min(1) @Max(6)
    private Integer estrato;

    @PositiveOrZero
    private Integer areaM2;

    @PositiveOrZero
    private Integer habitaciones;

    @PositiveOrZero
    private Integer banos;

    @PositiveOrZero
    private Integer parqueaderos;

    @ElementCollection
    @CollectionTable(name = "inmueble_fotos", joinColumns = @JoinColumn(name = "inmueble_id"))
    @OrderColumn(name = "orden")
    @Column(name = "archivo")
    private List<String> fotos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario propietario;

    private boolean activo = true;

    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    public String getFotoPrincipal() { return fotos.isEmpty() ? null : fotos.get(0); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TipoInmueble getTipo() { return tipo; }
    public void setTipo(TipoInmueble tipo) { this.tipo = tipo; }
    public Operacion getOperacion() { return operacion; }
    public void setOperacion(Operacion operacion) { this.operacion = operacion; }
    public Long getPrecio() { return precio; }
    public void setPrecio(Long precio) { this.precio = precio; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }
    public Integer getEstrato() { return estrato; }
    public void setEstrato(Integer estrato) { this.estrato = estrato; }
    public Integer getAreaM2() { return areaM2; }
    public void setAreaM2(Integer areaM2) { this.areaM2 = areaM2; }
    public Integer getHabitaciones() { return habitaciones; }
    public void setHabitaciones(Integer habitaciones) { this.habitaciones = habitaciones; }
    public Integer getBanos() { return banos; }
    public void setBanos(Integer banos) { this.banos = banos; }
    public Integer getParqueaderos() { return parqueaderos; }
    public void setParqueaderos(Integer parqueaderos) { this.parqueaderos = parqueaderos; }
    public List<String> getFotos() { return fotos; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }
    public Usuario getPropietario() { return propietario; }
    public void setPropietario(Usuario propietario) { this.propietario = propietario; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
}

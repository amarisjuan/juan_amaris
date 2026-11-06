package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
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

    // --- Ubicación en el mapa (opcional, rango aproximado de Colombia) ---
    @DecimalMin(value = "-5", message = "Ubicación fuera de Colombia") @DecimalMax(value = "14", message = "Ubicación fuera de Colombia")
    private Double latitud;

    @DecimalMin(value = "-82", message = "Ubicación fuera de Colombia") @DecimalMax(value = "-66", message = "Ubicación fuera de Colombia")
    private Double longitud;

    // --- Zona pública aproximada (lo único que ve el público). Se calcula una vez al guardar la ubicación. ---
    private Double latitudPublica;
    private Double longitudPublica;

    // --- Para estudiantes ---
    @ManyToOne
    private Universidad universidadCercana;

    @Min(value = 1, message = "Mínimo 1 minuto") @Max(value = 120, message = "Máximo 120 minutos")
    private Integer minutosAPie;

    @ColumnDefault("false")
    private boolean amoblado;

    @ColumnDefault("false")
    private boolean serviciosIncluidos;

    // --- Reglas de la vivienda ---
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'MIXTO'")
    private Convivencia convivencia = Convivencia.MIXTO;

    // Valores por defecto para que las bases existentes se actualicen sin errores
    @ColumnDefault("false")
    private boolean permiteMascotas;
    @ColumnDefault("false")
    private boolean permiteFumar;
    @ColumnDefault("true")
    private boolean permiteVisitas = true;

    @Size(max = 60, message = "Máximo 60 caracteres")
    private String horarioLlegada;

    @Size(max = 500, message = "Máximo 500 caracteres")
    @Column(length = 500)
    private String otrasReglas;

    @ElementCollection
    @CollectionTable(name = "inmueble_fotos", joinColumns = @JoinColumn(name = "inmueble_id"))
    @OrderColumn(name = "orden")
    @Column(name = "archivo")
    private List<String> fotos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario propietario;


    // --- Revisión por reportes (consulta legal 003). Oculto al público mientras está en revisión. ---
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean enRevision;
    private String motivoRevision;
    @Column(length = 2000)
    private String respuestaAnunciante;
    private java.time.LocalDateTime fechaRevision;

    private boolean activo = true;

    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    public String getFotoPrincipal() { return fotos.isEmpty() ? null : fotos.get(0); }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public boolean isUbicado() { return latitud != null && longitud != null; }
    public Double getLatitudPublica() { return latitudPublica; }
    public Double getLongitudPublica() { return longitudPublica; }
    public void setZonaPublica(Double lat, Double lng) { this.latitudPublica = lat; this.longitudPublica = lng; }
    public boolean isZonaPublicaLista() { return latitudPublica != null && longitudPublica != null; }

    /** Minutos a pie redondeados hacia arriba de 5 en 5, para no permitir triangular la ubicación. */
    public Integer getMinutosAPieAprox() {
        return minutosAPie == null ? null : Math.max(5, (int) Math.ceil(minutosAPie / 5.0) * 5);
    }

    public Universidad getUniversidadCercana() { return universidadCercana; }
    public void setUniversidadCercana(Universidad universidadCercana) { this.universidadCercana = universidadCercana; }
    public Integer getMinutosAPie() { return minutosAPie; }
    public void setMinutosAPie(Integer minutosAPie) { this.minutosAPie = minutosAPie; }
    public boolean isAmoblado() { return amoblado; }
    public void setAmoblado(boolean amoblado) { this.amoblado = amoblado; }
    public boolean isServiciosIncluidos() { return serviciosIncluidos; }
    public void setServiciosIncluidos(boolean serviciosIncluidos) { this.serviciosIncluidos = serviciosIncluidos; }

    public Convivencia getConvivencia() { return convivencia; }
    public void setConvivencia(Convivencia convivencia) { this.convivencia = convivencia == null ? Convivencia.MIXTO : convivencia; }
    public boolean isPermiteMascotas() { return permiteMascotas; }
    public void setPermiteMascotas(boolean permiteMascotas) { this.permiteMascotas = permiteMascotas; }
    public boolean isPermiteFumar() { return permiteFumar; }
    public void setPermiteFumar(boolean permiteFumar) { this.permiteFumar = permiteFumar; }
    public boolean isPermiteVisitas() { return permiteVisitas; }
    public void setPermiteVisitas(boolean permiteVisitas) { this.permiteVisitas = permiteVisitas; }
    public String getHorarioLlegada() { return horarioLlegada; }
    public void setHorarioLlegada(String horarioLlegada) { this.horarioLlegada = vacioANull(horarioLlegada); }
    public String getOtrasReglas() { return otrasReglas; }
    public void setOtrasReglas(String otrasReglas) { this.otrasReglas = vacioANull(otrasReglas); }

    private static String vacioANull(String s) { return s == null || s.isBlank() ? null : s.trim(); }

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
    public void setBarrio(String barrio) { this.barrio = barrio == null || barrio.isBlank() ? null : barrio.trim(); }
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

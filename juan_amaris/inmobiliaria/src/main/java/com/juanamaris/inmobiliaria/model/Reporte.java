package com.juanamaris.inmobiliaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Reporte confidencial de un anuncio o perfil. Nunca se muestra en el sitio: solo lo ve el administrador.
 * Al reportado no se le revela quién reportó (consulta legal 003).
 */
@Entity
@Table(name = "reportes", uniqueConstraints = @UniqueConstraint(columnNames = {"reportante_id", "objetivo", "objetivo_id"}))
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario reportante;

    /** Dueño del anuncio o perfil reportado. */
    @ManyToOne(optional = false)
    private Usuario reportado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjetivoReporte objetivo;

    @Column(name = "objetivo_id", nullable = false)
    private Long objetivoId;

    @NotNull(message = "Elige un motivo")
    @Enumerated(EnumType.STRING)
    private MotivoReporte motivo;

    @NotBlank(message = "Describe lo que pasó")
    @Size(max = 1000, message = "Máximo 1000 caracteres")
    @Column(length = 1000)
    private String descripcion;

    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ResultadoReporte resultado;

    @Size(max = 500)
    @Column(length = 500)
    private String notaResolucion;

    private LocalDateTime fechaResolucion;

    public boolean isPendiente() { return resultado == null; }

    public Long getId() { return id; }
    public Usuario getReportante() { return reportante; }
    public void setReportante(Usuario reportante) { this.reportante = reportante; }
    public Usuario getReportado() { return reportado; }
    public void setReportado(Usuario reportado) { this.reportado = reportado; }
    public ObjetivoReporte getObjetivo() { return objetivo; }
    public void setObjetivo(ObjetivoReporte objetivo) { this.objetivo = objetivo; }
    public Long getObjetivoId() { return objetivoId; }
    public void setObjetivoId(Long objetivoId) { this.objetivoId = objetivoId; }
    public MotivoReporte getMotivo() { return motivo; }
    public void setMotivo(MotivoReporte motivo) { this.motivo = motivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFecha() { return fecha; }
    public ResultadoReporte getResultado() { return resultado; }
    public String getNotaResolucion() { return notaResolucion; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }

    public void resolver(ResultadoReporte resultado, String nota) {
        this.resultado = resultado;
        this.notaResolucion = nota;
        this.fechaResolucion = LocalDateTime.now();
    }

    @Override
    public String toString() { return "Reporte[id=" + id + "]"; }
}

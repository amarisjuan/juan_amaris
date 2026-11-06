package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.*;
import com.juanamaris.inmobiliaria.repository.CompaneroRepository;
import com.juanamaris.inmobiliaria.repository.InmuebleRepository;
import com.juanamaris.inmobiliaria.repository.ReporteRepository;
import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Reportes confidenciales (consulta legal 003): solo con sesión, uno por persona y objetivo, máximo N al día.
 * Si 3 cuentas distintas reportan el mismo anuncio por motivos graves en 30 días, se oculta mientras se revisa.
 */
@Service
public class ReporteService {

    public static final List<MotivoReporte> GRAVES = Arrays.stream(MotivoReporte.values()).filter(MotivoReporte::isGrave).toList();

    private final ReporteRepository repo;
    private final InmuebleRepository inmuebles;
    private final CompaneroRepository companeros;
    private final UsuarioRepository usuarios;
    private final int maxPorDia, umbral;

    public ReporteService(ReporteRepository repo, InmuebleRepository inmuebles, CompaneroRepository companeros,
                          UsuarioRepository usuarios,
                          @Value("${app.reportes.max-por-dia}") int maxPorDia,
                          @Value("${app.reportes.umbral-ocultar}") int umbral) {
        this.repo = repo;
        this.inmuebles = inmuebles;
        this.companeros = companeros;
        this.usuarios = usuarios;
        this.maxPorDia = maxPorDia;
        this.umbral = umbral;
    }

    /** Dueño del objetivo, validando que exista y sea visible. */
    @Transactional(readOnly = true)
    public Usuario duenoDe(ObjetivoReporte objetivo, Long id) {
        return switch (objetivo) {
            case INMUEBLE -> inmuebles.findById(id).map(Inmueble::getPropietario).orElse(null);
            case COMPANERO -> companeros.findById(id).map(Companero::getAutor).orElse(null);
            case PERFIL -> usuarios.findById(id).orElse(null);
        };
    }

    @Transactional(readOnly = true)
    public String tituloDe(ObjetivoReporte objetivo, Long id) {
        return switch (objetivo) {
            case INMUEBLE -> inmuebles.findById(id).map(Inmueble::getTitulo).orElse("(eliminado)");
            case COMPANERO -> companeros.findById(id).map(c -> c.getTipo().getEtiqueta() + " · " + c.getUniversidad().getNombreCorto()).orElse("(eliminado)");
            case PERFIL -> usuarios.findById(id).map(Usuario::getNombrePublico).orElse("(eliminado)");
        };
    }

    /** @return true si con este reporte el anuncio quedó oculto para revisión. */
    @Transactional
    public boolean reportar(Usuario reportante, ObjetivoReporte objetivo, Long id, MotivoReporte motivo, String descripcion) {
        Usuario dueno = duenoDe(objetivo, id);
        if (dueno == null) throw new IllegalArgumentException("No encontramos lo que quieres reportar");
        if (dueno.getId().equals(reportante.getId())) throw new IllegalArgumentException("No puedes reportar tu propia publicación");
        if (repo.existsByReportanteAndObjetivoAndObjetivoId(reportante, objetivo, id)) {
            throw new IllegalArgumentException("Ya reportaste esta publicación. Lo estamos revisando.");
        }
        if (repo.countByReportanteAndFechaAfter(reportante, LocalDateTime.now().minusDays(1)) >= maxPorDia) {
            throw new IllegalArgumentException("Alcanzaste el máximo de " + maxPorDia + " reportes por día.");
        }
        Reporte r = new Reporte();
        r.setReportante(reportante);
        r.setReportado(dueno);
        r.setObjetivo(objetivo);
        r.setObjetivoId(id);
        r.setMotivo(motivo);
        r.setDescripcion(descripcion.trim());
        repo.save(r);

        if (!motivo.isGrave() || objetivo == ObjetivoReporte.PERFIL) return false;
        long cuentas = repo.reportantesDistintos(objetivo, id, GRAVES, LocalDateTime.now().minusDays(30));
        if (cuentas < umbral) return false;
        String general = "Recibimos reportes sobre este anuncio (" + motivo.getEtiqueta().toLowerCase() + ")";
        if (objetivo == ObjetivoReporte.INMUEBLE) inmuebles.findById(id).filter(i -> !i.isEnRevision()).ifPresent(i -> i.ponerEnRevision(general));
        else companeros.findById(id).filter(c -> !c.isEnRevision()).ifPresent(c -> c.ponerEnRevision(general));
        return true;
    }

    // ---- Administración ----

    public record FilaReportado(Usuario usuario, long total, long pendientes, long graves, LocalDateTime ultimo) {}

    @Transactional(readOnly = true)
    public List<FilaReportado> resumen() {
        return repo.resumenPorReportado(GRAVES).stream().map(f -> new FilaReportado((Usuario) f[0],
                ((Number) f[1]).longValue(), ((Number) f[2]).longValue(), ((Number) f[3]).longValue(), (LocalDateTime) f[4])).toList();
    }

    @Transactional(readOnly = true)
    public List<Reporte> de(Usuario reportado) { return repo.findByReportadoOrderByFechaDesc(reportado); }

    @Transactional(readOnly = true)
    public long pendientes() { return repo.countByResultadoIsNull(); }

    @Transactional
    public void resolver(Long reporteId, ResultadoReporte resultado, String nota) {
        repo.findById(reporteId).orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado")).resolver(resultado, nota);
    }

    @Transactional
    public void ocultar(ObjetivoReporte objetivo, Long id, String motivoGeneral) {
        if (objetivo == ObjetivoReporte.INMUEBLE) inmuebles.findById(id).ifPresent(i -> i.ponerEnRevision(motivoGeneral));
        if (objetivo == ObjetivoReporte.COMPANERO) companeros.findById(id).ifPresent(c -> c.ponerEnRevision(motivoGeneral));
    }

    @Transactional
    public void restaurar(ObjetivoReporte objetivo, Long id) {
        if (objetivo == ObjetivoReporte.INMUEBLE) inmuebles.findById(id).ifPresent(Inmueble::quitarRevision);
        if (objetivo == ObjetivoReporte.COMPANERO) companeros.findById(id).ifPresent(Companero::quitarRevision);
    }
}

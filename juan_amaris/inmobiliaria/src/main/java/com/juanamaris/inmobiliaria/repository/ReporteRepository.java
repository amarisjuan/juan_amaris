package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    boolean existsByReportanteAndObjetivoAndObjetivoId(Usuario reportante, ObjetivoReporte objetivo, Long objetivoId);

    long countByReportanteAndFechaAfter(Usuario reportante, LocalDateTime desde);

    long countByResultadoIsNull();

    @Query("select count(distinct r.reportante.id) from Reporte r where r.objetivo = :objetivo and r.objetivoId = :id "
            + "and r.motivo in :motivos and r.fecha >= :desde")
    long reportantesDistintos(ObjetivoReporte objetivo, Long id, Collection<MotivoReporte> motivos, LocalDateTime desde);

    List<Reporte> findByReportadoOrderByFechaDesc(Usuario reportado);

    /** [usuario reportado, total, pendientes, graves, último reporte] */
    @Query("select r.reportado, count(r), sum(case when r.resultado is null then 1 else 0 end), "
            + "sum(case when r.motivo in :graves then 1 else 0 end), max(r.fecha) "
            + "from Reporte r group by r.reportado order by sum(case when r.resultado is null then 1 else 0 end) desc, count(r) desc")
    List<Object[]> resumenPorReportado(Collection<MotivoReporte> graves);
}

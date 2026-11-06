package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.ConteoDiario;
import com.juanamaris.inmobiliaria.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConteoDiarioRepository extends JpaRepository<ConteoDiario, Long> {

    Optional<ConteoDiario> findByFechaAndTipoAndClave(LocalDate fecha, TipoEvento tipo, String clave);

    @Query("select coalesce(sum(c.cantidad), 0) from ConteoDiario c where c.tipo in :tipos and c.fecha >= :desde")
    long total(Collection<TipoEvento> tipos, LocalDate desde);

    /** [fecha, cantidad] por día. */
    @Query("select c.fecha, sum(c.cantidad) from ConteoDiario c where c.tipo in :tipos and c.fecha >= :desde group by c.fecha")
    List<Object[]> porDia(Collection<TipoEvento> tipos, LocalDate desde);

    /** [clave, cantidad] por clave, de mayor a menor. */
    @Query("select c.clave, sum(c.cantidad) from ConteoDiario c where c.tipo in :tipos and c.fecha >= :desde "
            + "group by c.clave order by sum(c.cantidad) desc")
    List<Object[]> porClave(Collection<TipoEvento> tipos, LocalDate desde);
}

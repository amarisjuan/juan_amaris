package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.Universidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UniversidadRepository extends JpaRepository<Universidad, Long> {

    List<Universidad> findAllByOrderByCiudadAscNombreAsc();

    Optional<Universidad> findBySigla(String sigla);

    /** [universidadId, cantidad de inmuebles activos cerca]. */
    @Query("select i.universidadCercana.id, count(i) from Inmueble i "
            + "where i.activo = true and i.universidadCercana is not null group by i.universidadCercana.id")
    List<Object[]> contarInmueblesCercanos();
}

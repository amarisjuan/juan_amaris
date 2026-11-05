package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface InmuebleRepository extends JpaRepository<Inmueble, Long>, JpaSpecificationExecutor<Inmueble> {

    List<Inmueble> findByPropietarioOrderByFechaPublicacionDesc(Usuario propietario);

    @EntityGraph(attributePaths = {"fotos", "propietario", "universidadCercana"})
    @Query("select i from Inmueble i where i.id = :id")
    Optional<Inmueble> findDetalleById(Long id);

    @Query("select distinct i.ciudad from Inmueble i where i.activo = true order by i.ciudad")
    List<String> ciudadesConInmuebles();
}

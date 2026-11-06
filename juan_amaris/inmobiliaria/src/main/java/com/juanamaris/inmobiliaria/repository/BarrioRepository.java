package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.Barrio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BarrioRepository extends JpaRepository<Barrio, Long> {
    boolean existsByCiudadAndNombre(String ciudad, String nombre);
    List<Barrio> findAllByOrderByCiudadAscNombreAsc();
}

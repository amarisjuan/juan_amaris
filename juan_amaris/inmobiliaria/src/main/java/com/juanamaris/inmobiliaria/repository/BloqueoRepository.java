package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {
    boolean existsByTipoAndValor(Bloqueo.Tipo tipo, String valor);
    Optional<Bloqueo> findByTipoAndValor(Bloqueo.Tipo tipo, String valor);
    List<Bloqueo> findAllByOrderByFechaDesc();
}

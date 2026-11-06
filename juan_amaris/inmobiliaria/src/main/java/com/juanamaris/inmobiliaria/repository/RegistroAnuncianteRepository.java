package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.RegistroAnunciante;
import com.juanamaris.inmobiliaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistroAnuncianteRepository extends JpaRepository<RegistroAnunciante, Long> {
    Optional<RegistroAnunciante> findByUsuario(Usuario usuario);
    boolean existsByUsuario(Usuario usuario);
    List<RegistroAnunciante> findAllByOrderByFechaRegistroDesc();
}

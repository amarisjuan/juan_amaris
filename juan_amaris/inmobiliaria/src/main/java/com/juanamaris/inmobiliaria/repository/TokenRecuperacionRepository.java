package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.TokenRecuperacion;
import com.juanamaris.inmobiliaria.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<TokenRecuperacion> findByHash(String hash);

    long countByUsuarioAndCreadoEnAfter(Usuario usuario, LocalDateTime desde);

    List<TokenRecuperacion> findByUsuarioAndUsadoEnIsNull(Usuario usuario);

    void deleteByVenceEnBefore(LocalDateTime limite);
}

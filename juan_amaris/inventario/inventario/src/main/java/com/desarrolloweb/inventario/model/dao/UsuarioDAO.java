package com.desarrolloweb.inventario.model.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.desarrolloweb.inventario.model.entity.Usuario;

public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
